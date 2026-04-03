package com.aih.zpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureTagQueryRequest implements Serializable {

    private String tagName;

    private static final long serialVersionUID = 1L;
}
