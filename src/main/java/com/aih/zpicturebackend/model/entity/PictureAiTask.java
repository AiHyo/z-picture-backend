package com.aih.zpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "picture_ai_task")
@Data
public class PictureAiTask implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pictureId;

    private Long userId;

    private String taskType;

    private String externalTaskId;

    private Integer taskStatus;

    private String requestParams;

    private String resultUrl;

    private String errorMessage;

    private Date finishTime;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
