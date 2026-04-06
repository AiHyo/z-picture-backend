package com.aih.zpicturebackend.api.imagesearch.baidu;

import com.aih.zpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class BaiduImageSearchClientTest {

    @Test
    void extractCardDataShouldParseNestedJsonFromHtml() {
        String html = "<html><script>window.cardData = [{\"cardName\":\"same\",\"tplData\":{\"list\":[{\"title\":[\"same\"],"
                + "\"image_src\":\"https://img.example.com/same.jpg\",\"url\":\"https://source.example.com/a\"}]}}];</script></html>";

        List<JsonNode> cardData = BaiduImageSearchClient.extractCardData(html);

        Assertions.assertEquals(1, cardData.size());
        Assertions.assertEquals("same", cardData.get(0).path("cardName").asText());
    }

    @Test
    void parseCardDataShouldCollectExactAndSimilarMatches() throws Exception {
        String htmlJson = "["
                + "{\"cardName\":\"same\",\"tplData\":{\"list\":[{\"title\":[\"exact\"],\"image_src\":\"https://img.example.com/exact.jpg\","
                + "\"url\":\"https://source.example.com/exact\"}]}},"
                + "{\"cardName\":\"simipic\",\"tplData\":{\"firstUrl\":\"https://graph.baidu.com/ajax/pcsimi?id=1\"}}"
                + "]";
        List<JsonNode> cardData = BaiduImageSearchClient.extractCardData("window.cardData = " + htmlJson);

        BaiduImageSearchResponse response = BaiduImageSearchClient.parseCardData(cardData,
                "https://graph.baidu.com/result",
                url -> "{\"data\":{\"list\":[{\"thumbUrl\":\"https://img.example.com/similar.jpg\","
                        + "\"fromUrl\":\"https://source.example.com/similar\"}]}}");

        Assertions.assertEquals(1, response.getExactMatches().size());
        Assertions.assertEquals(1, response.getSimilarMatches().size());
        Assertions.assertEquals("https://graph.baidu.com/result", response.getResultPageUrl());
    }

    @Test
    void toImageSearchResultsShouldKeepOrderAndRemoveDuplicates() {
        BaiduImageSearchItem exactMatch = new BaiduImageSearchItem("exact",
                "https://img.example.com/shared.jpg", "https://source.example.com/shared");
        BaiduImageSearchItem duplicatedSimilar = new BaiduImageSearchItem("similar",
                "https://img.example.com/shared.jpg", "https://source.example.com/shared");
        BaiduImageSearchItem uniqueSimilar = new BaiduImageSearchItem("similar2",
                "https://img.example.com/other.jpg", "https://source.example.com/other");
        BaiduImageSearchResponse response = new BaiduImageSearchResponse(
                Arrays.asList(duplicatedSimilar, uniqueSimilar),
                Arrays.asList(exactMatch),
                "https://graph.baidu.com/result");

        List<ImageSearchResult> results = BaiduImageSearchClient.toImageSearchResults(response);

        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals("https://img.example.com/shared.jpg", results.get(0).getThumbUrl());
        Assertions.assertEquals("https://img.example.com/other.jpg", results.get(1).getThumbUrl());
    }

    @Test
    void parseCardDataShouldReturnEmptyWhenNoResultCardExists() throws Exception {
        List<JsonNode> cardData = BaiduImageSearchClient.extractCardData("window.cardData = [{\"cardName\":\"noresult\"}]");

        BaiduImageSearchResponse response = BaiduImageSearchClient.parseCardData(cardData,
                "https://graph.baidu.com/result",
                url -> "");

        Assertions.assertTrue(response.getExactMatches().isEmpty());
        Assertions.assertTrue(response.getSimilarMatches().isEmpty());
    }
}
