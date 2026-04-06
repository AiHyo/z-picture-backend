package com.aih.zpicturebackend.api.imagesearch.baidu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 百度搜图响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaiduImageSearchResponse {

    /**
     * 相似图片列表
     */
    private List<BaiduImageSearchItem> similarMatches;

    /**
     * 原图来源列表
     */
    private List<BaiduImageSearchItem> exactMatches;

    /**
     * 百度结果页地址
     */
    private String resultPageUrl;
}
