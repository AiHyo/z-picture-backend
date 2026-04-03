package com.aih.zpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureCategoryAddRequest implements Serializable {

    private String categoryName;

    private static final long serialVersionUID = 1L;
}
