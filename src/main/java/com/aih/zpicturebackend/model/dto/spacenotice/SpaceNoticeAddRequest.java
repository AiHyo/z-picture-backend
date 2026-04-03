package com.aih.zpicturebackend.model.dto.spacenotice;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceNoticeAddRequest implements Serializable {

    private Long spaceId;

    private String title;

    private String content;

    private Integer isPinned;

    private static final long serialVersionUID = 1L;
}
