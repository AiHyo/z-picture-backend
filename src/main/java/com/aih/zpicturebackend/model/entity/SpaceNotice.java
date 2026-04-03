package com.aih.zpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "space_notice")
@Data
public class SpaceNotice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spaceId;

    private Long userId;

    private String title;

    private String content;

    private Integer isPinned;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
