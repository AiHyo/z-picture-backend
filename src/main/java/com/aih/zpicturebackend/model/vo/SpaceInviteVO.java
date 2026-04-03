package com.aih.zpicturebackend.model.vo;

import com.aih.zpicturebackend.model.entity.SpaceInvite;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class SpaceInviteVO implements Serializable {

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

    private UserVO inviter;

    private UserVO invitee;

    private static final long serialVersionUID = 1L;

    public static SpaceInviteVO objToVo(SpaceInvite spaceInvite) {
        if (spaceInvite == null) {
            return null;
        }
        SpaceInviteVO spaceInviteVO = new SpaceInviteVO();
        BeanUtils.copyProperties(spaceInvite, spaceInviteVO);
        return spaceInviteVO;
    }
}
