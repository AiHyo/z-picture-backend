package com.aih.zpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureCategoryEditRequest implements Serializable {

    private Long id;

    private String categoryName;

    private static final long serialVersionUID = 1L;
}
