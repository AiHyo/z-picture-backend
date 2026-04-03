package com.aih.zpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureReportAddRequest implements Serializable {

    private Long pictureId;

    private String reportReasonType;

    private String reportReasonText;

    private static final long serialVersionUID = 1L;
}
