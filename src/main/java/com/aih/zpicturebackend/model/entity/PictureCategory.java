package com.aih.zpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "picture_category")
@Data
public class PictureCategory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String categoryName;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
