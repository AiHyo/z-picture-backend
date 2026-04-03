package com.aih.zpicturebackend.model.dto.picture;

import com.aih.zpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PictureReportQueryRequest extends PageRequest implements Serializable {

    private Long pictureId;

    private Integer reportStatus;

    private static final long serialVersionUID = 1L;
}
