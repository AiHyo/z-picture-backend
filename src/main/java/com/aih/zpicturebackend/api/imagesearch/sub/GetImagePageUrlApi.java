package com.aih.zpicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取以图搜图页面地址（step1）
 */
@Slf4j
public class GetImagePageUrlApi {

    public static String getImagePageUrl(String imageUrl) {
        HashMap<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
// 请求头 acs-token:
// 1742185780288_1742215928444_5P5YeLXmC48IbpScV4pfN/Ve9uEcgtasmmpYyHV6eqehobnA/zeuw8R2HeTj4kB74Ed8aKb+/Cwrdwmz7WxDY4bsrRDoY/LormZlLkIE1ms6vJgt+LUE52MWMgajH8Lazhx73Uyg1iqi1vNhc39NYMSDQ6llhzHZPM0oKSHxjzkABQ9xRzJ6fN06r793KCcVacs/5NGTG4e36DWL1VW93qBv4w+BtI1uigVr6/1YFkDfiUvfbGnASlJPGKO/Kgboq1rEVBnQgOe5btG8ETwBGKtIXtkm3lOc+tq65+H0wrNPeMKibZx6Q4FZxhWmpLpfiroJuYf6mE0RzysAGYnjTVl6IL4+kD00p30kUZOCEUAwcKo/ERvFphdgapulnlu02UQUO5sZzh9Bvg9YPj1AT4VySOyKUILTB3dyBJkhQgwkUI4xpuO0y7AKZGmmN5Uo
        HashMap<String, String> headers = new HashMap<>();
        headers.put("acs-token", "1742185780288_1742215928444_5P5YeLXmC48IbpScV4pfN/Ve9uEcgtasmmpYyHV6eqehobnA/zeuw8R2HeTj4kB74Ed8aKb+/Cwrdwmz7WxDY4bsrDoY/LormZlLkIE1ms6vJgt+LUE52MWMgajH8Lazhx73Uyg1iqi1vNhc39NYMSDQ6llhzHZPM0oKSHxjzkABQ9xRzJ6fN06r793KCcVacs/5NGTG4e36DWL1VW93qBv4w+BtI1uigVr6/1YFkDfiUvfbGnASlJPGKO/Kgboq1rEVBnQgOe5btG8ETwBGKtIXtkm3lOc+tq65+H0wrNPeMKibZx6Q4FZxhWmpLpfiroJuYf6mE0RzysAGYnjTVl6IL4+kD00p30kUZOCEUAwcKo/ERvFphdgapulnlu02UQUO5sZzh9Bvg9YPj1AT4VySOyKUILTB3dyBJkhQgwkUI4xpuO0y7AKZGmmN5Uo");
        try {
            // 2. 发送 POST 请求到百度接口
            HttpResponse response = HttpRequest.post(url)
                    .form(formData)
                    .timeout(5000)
                    .addHeaders(headers)
                    .execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 获取response.body() 并转换为 Map
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);
            // 3. 解析出 ['data']['url']
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码 并返回
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("调用百度以图搜图接口失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        String imageUrl = "https://tse4-mm.cn.bing.net/th/id/OIP-C.DKnLiYmKDHfiF2rk-kbwrgHaKl?w=202&h=288&c=7&r=0&o=5&dpr=1.3&pid=1.7";
        String searchResultUrl = getImagePageUrl(imageUrl);
        System.out.println(searchResultUrl);
    }



}
