package com.aih.zpicturebackend.model.dto.spacenotice;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceNoticeQueryRequest implements Serializable {

    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
