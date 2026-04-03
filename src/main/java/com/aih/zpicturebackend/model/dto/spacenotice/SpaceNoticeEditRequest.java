package com.aih.zpicturebackend.model.dto.spacenotice;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceNoticeEditRequest implements Serializable {

    private Long id;

    private Long spaceId;

    private String title;

    private String content;

    private Integer isPinned;

    private static final long serialVersionUID = 1L;
}
