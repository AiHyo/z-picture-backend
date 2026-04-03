package com.aih.zpicturebackend.model.dto.spaceinvite;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceInviteAddRequest implements Serializable {

    private Long spaceId;

    private Long inviteeId;

    private String spaceRole;

    private String inviteMessage;

    private static final long serialVersionUID = 1L;
}
