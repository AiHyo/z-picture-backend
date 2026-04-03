package com.aih.zpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "space_invite")
@Data
public class SpaceInvite implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spaceId;

    private Long inviterId;

    private Long inviteeId;

    private String spaceRole;

    private String inviteMessage;

    private Integer inviteStatus;

    private Date handleTime;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
