package com.aih.zpicturebackend.api.imagesearch.baidu;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 百度搜图结果项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaiduImageSearchItem {

    /**
     * 图片标题
     */
    private String title;

    /**
     * 缩略图地址
     */
    private String thumbnail;

    /**
     * 来源地址
     */
    private String url;

    static BaiduImageSearchItem fromJson(JsonNode data) {
        String title = "";
        JsonNode titleNode = data.get("title");
        if (titleNode != null && titleNode.isArray() && titleNode.size() > 0) {
            title = titleNode.get(0).asText("");
        } else if (titleNode != null && titleNode.isTextual()) {
            title = titleNode.asText("");
        }

        String thumbnail = "";
        if (data.has("image_src")) {
            thumbnail = data.get("image_src").asText("");
        } else if (data.has("thumbUrl")) {
            thumbnail = data.get("thumbUrl").asText("");
        }

        String url = "";
        if (data.has("url")) {
            url = data.get("url").asText("");
        } else if (data.has("fromUrl")) {
            url = data.get("fromUrl").asText("");
        }

        return new BaiduImageSearchItem(title, thumbnail, url);
    }
}
