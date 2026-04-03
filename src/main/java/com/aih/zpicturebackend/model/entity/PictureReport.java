package com.aih.zpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "picture_report")
@Data
public class PictureReport implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pictureId;

    private Long reporterId;

    private String reportReasonType;

    private String reportReasonText;

    private Integer reportStatus;

    private Long processorId;

    private String processResult;

    private Date processTime;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
