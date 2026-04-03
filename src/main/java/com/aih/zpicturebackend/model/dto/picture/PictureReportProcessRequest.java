package com.aih.zpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureReportProcessRequest implements Serializable {

    private Long id;

    private Integer reportStatus;

    private String processResult;

    private static final long serialVersionUID = 1L;
}
