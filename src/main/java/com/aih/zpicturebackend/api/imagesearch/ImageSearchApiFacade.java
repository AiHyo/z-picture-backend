package com.aih.zpicturebackend.api.imagesearch;

import com.aih.zpicturebackend.api.imagesearch.baidu.BaiduImageSearchClient;
import com.aih.zpicturebackend.api.imagesearch.model.ImageSearchResult;

import java.util.List;

public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        return BaiduImageSearchClient.search(imageUrl);
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://www.codefather.cn/logo.png";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
