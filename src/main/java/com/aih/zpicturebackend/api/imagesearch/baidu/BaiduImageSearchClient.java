package com.aih.zpicturebackend.api.imagesearch.baidu;

import com.aih.zpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 百度以图搜图客户端
 */
@Slf4j
public final class BaiduImageSearchClient {

    private static final String BASE_URL = "https://graph.baidu.com";
    private static final String UPLOAD_URL = BASE_URL + "/upload";
    private static final int TIMEOUT_MILLIS = 10000;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private BaiduImageSearchClient() {
    }

    /**
     * 通过图片公网地址搜图
     */
    public static List<ImageSearchResult> search(String imageUrl) {
        try {
            byte[] imageBytes = downloadImage(imageUrl);
            BaiduImageSearchResponse response = searchInternal(imageBytes, BaiduImageSearchClient::fetchPageContent);
            return toImageSearchResults(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Baidu image search failed, imageUrl={}", imageUrl, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "以图搜图失败");
        }
    }

    static BaiduImageSearchResponse searchInternal(byte[] imageBytes, PageContentFetcher pageContentFetcher)
            throws IOException {
        String resultPageUrl = uploadImage(imageBytes);
        if (isBlank(resultPageUrl)) {
            return emptyResponse();
        }
        String htmlContent = pageContentFetcher.fetch(resultPageUrl);
        List<JsonNode> cardData = extractCardData(htmlContent);
        return parseCardData(cardData, resultPageUrl, pageContentFetcher);
    }

    static List<JsonNode> extractCardData(String html) {
        if (isBlank(html)) {
            return Collections.emptyList();
        }
        String keyword = "window.cardData";
        int keywordIndex = html.indexOf(keyword);
        if (keywordIndex < 0) {
            return Collections.emptyList();
        }

        int start = html.indexOf('[', keywordIndex);
        if (start < 0) {
            return Collections.emptyList();
        }

        int balance = 0;
        int end = -1;
        boolean inString = false;
        boolean escaping = false;
        for (int i = start; i < html.length(); i++) {
            char c = html.charAt(i);
            if (inString) {
                if (escaping) {
                    escaping = false;
                } else if (c == '\\') {
                    escaping = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '[') {
                balance++;
            } else if (c == ']') {
                balance--;
                if (balance == 0) {
                    end = i + 1;
                    break;
                }
            }
        }

        if (end < 0) {
            return Collections.emptyList();
        }

        try {
            String jsonArray = html.substring(start, end);
            return OBJECT_MAPPER.readValue(jsonArray, new TypeReference<List<JsonNode>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse cardData from Baidu result page", e);
            return Collections.emptyList();
        }
    }

    static BaiduImageSearchResponse parseCardData(List<JsonNode> cardData, String resultPageUrl,
                                                  PageContentFetcher pageContentFetcher) throws IOException {
        if (cardData == null || cardData.isEmpty()) {
            return emptyResponse(resultPageUrl);
        }

        List<BaiduImageSearchItem> exactMatches = new ArrayList<>();
        List<BaiduImageSearchItem> similarMatches = new ArrayList<>();

        for (JsonNode card : cardData) {
            String cardName = card.path("cardName").asText("");
            if ("noresult".equals(cardName)) {
                return emptyResponse(resultPageUrl);
            }

            JsonNode tplData = card.get("tplData");
            if (tplData == null || tplData.isNull()) {
                continue;
            }

            if ("same".equals(cardName)) {
                JsonNode listNode = tplData.get("list");
                if (listNode != null && listNode.isArray()) {
                    for (JsonNode item : listNode) {
                        exactMatches.add(BaiduImageSearchItem.fromJson(item));
                    }
                }
            }

            if ("simipic".equals(cardName)) {
                String firstUrl = normalizeUrl(tplData.path("firstUrl").asText(""));
                if (isBlank(firstUrl)) {
                    continue;
                }
                try {
                    String simiJson = pageContentFetcher.fetch(firstUrl);
                    JsonNode simiRoot = OBJECT_MAPPER.readTree(simiJson);
                    JsonNode listNode = simiRoot.path("data").path("list");
                    if (listNode.isArray()) {
                        for (JsonNode item : listNode) {
                            similarMatches.add(BaiduImageSearchItem.fromJson(item));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch similar image details, firstUrl={}", firstUrl, e);
                }
            }
        }

        return new BaiduImageSearchResponse(similarMatches, exactMatches, resultPageUrl);
    }

    static List<ImageSearchResult> toImageSearchResults(BaiduImageSearchResponse response) {
        if (response == null) {
            return Collections.emptyList();
        }
        Map<String, ImageSearchResult> deduplicatedResults = new LinkedHashMap<>();
        appendResults(deduplicatedResults, response.getExactMatches());
        appendResults(deduplicatedResults, response.getSimilarMatches());
        return new ArrayList<>(deduplicatedResults.values());
    }

    private static void appendResults(Map<String, ImageSearchResult> container, List<BaiduImageSearchItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (BaiduImageSearchItem item : items) {
            String thumbUrl = item == null ? "" : defaultString(item.getThumbnail());
            String fromUrl = item == null ? "" : defaultString(item.getUrl());
            if (isBlank(thumbUrl) && isBlank(fromUrl)) {
                continue;
            }
            String deduplicateKey = thumbUrl + "||" + fromUrl;
            if (container.containsKey(deduplicateKey)) {
                continue;
            }
            ImageSearchResult result = new ImageSearchResult();
            result.setThumbUrl(thumbUrl);
            result.setFromUrl(fromUrl);
            container.put(deduplicateKey, result);
        }
    }

    private static byte[] downloadImage(String imageUrl) throws IOException {
        if (isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "原图地址不存在");
        }
        HttpURLConnection connection = createConnection(imageUrl, "GET");
        int statusCode = connection.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "原图下载失败");
        }
        return readBytes(connection);
    }

    private static String uploadImage(byte[] imageBytes) throws IOException {
        String boundary = "---BaiduSearchBoundary" + System.currentTimeMillis();
        byte[] requestBody = buildMultipartRequestBody(boundary, imageBytes);
        HttpURLConnection connection = createConnection(UPLOAD_URL, "POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestProperty("Acs-Token", "");
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(requestBody);
        }

        int statusCode = connection.getResponseCode();
        String responseBody = readResponseBody(connection, statusCode);
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传搜图请求失败");
        }

        JsonNode root = OBJECT_MAPPER.readTree(responseBody);
        return normalizeUrl(root.path("data").path("url").asText(""));
    }

    private static String fetchPageContent(String url) throws IOException {
        HttpURLConnection connection = createConnection(url, "GET");
        int statusCode = connection.getResponseCode();
        String responseBody = readResponseBody(connection, statusCode);
        if (statusCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Unexpected response status: " + statusCode);
        }
        return responseBody;
    }

    private static HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MILLIS);
        connection.setReadTimeout(TIMEOUT_MILLIS);
        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        connection.setRequestProperty("Accept", "*/*");
        return connection;
    }

    private static byte[] buildMultipartRequestBody(String boundary, byte[] imageBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeFormField(outputStream, boundary, "from", "pc");
        writeFileField(outputStream, boundary, "image", "image.jpg", "image/jpeg", imageBytes);
        outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return outputStream.toByteArray();
    }

    private static void writeFormField(ByteArrayOutputStream outputStream, String boundary, String name, String value)
            throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static void writeFileField(ByteArrayOutputStream outputStream, String boundary, String name,
                                       String fileName, String contentType, byte[] imageBytes) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName
                + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(imageBytes);
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] readBytes(HttpURLConnection connection) throws IOException {
        try (InputStream inputStream = connection.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }

    private static String readResponseBody(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream inputStream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (inputStream == null) {
            return "";
        }
        try (InputStream stream = inputStream;
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static BaiduImageSearchResponse emptyResponse() {
        return emptyResponse("");
    }

    private static BaiduImageSearchResponse emptyResponse(String resultPageUrl) {
        return new BaiduImageSearchResponse(Collections.<BaiduImageSearchItem>emptyList(),
                Collections.<BaiduImageSearchItem>emptyList(), defaultString(resultPageUrl));
    }

    private static String normalizeUrl(String url) {
        if (isBlank(url)) {
            return "";
        }
        if (url.startsWith("//")) {
            return "https:" + url;
        }
        if (url.startsWith("/")) {
            return BASE_URL + url;
        }
        return url;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    interface PageContentFetcher {
        String fetch(String url) throws IOException;
    }
}
