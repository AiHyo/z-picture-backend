package com.aih.zpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureUploadByBatchRequest implements Serializable {

    private static final long serialVersionUID = 9062820951783884999L;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 抓取数量
     */
    private Integer count = 10;

    /**
     * 图片名称前缀
     */
    private String namePrefix;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 分类
     */
    private String category;
}
