package com.aih.zpicturebackend.model.vo;

import com.aih.zpicturebackend.model.entity.PictureReport;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class PictureReportVO implements Serializable {

    private Long id;

    private Long pictureId;

    private Long reporterId;

    private String reportReasonType;

    private String reportReasonText;

    private Integer reportStatus;

    private String processResult;

    private Date processTime;

    private Date createTime;

    private UserVO reporter;

    private static final long serialVersionUID = 1L;

    public static PictureReportVO objToVo(PictureReport pictureReport) {
        if (pictureReport == null) {
            return null;
        }
        PictureReportVO pictureReportVO = new PictureReportVO();
        BeanUtils.copyProperties(pictureReport, pictureReportVO);
        return pictureReportVO;
    }
}
