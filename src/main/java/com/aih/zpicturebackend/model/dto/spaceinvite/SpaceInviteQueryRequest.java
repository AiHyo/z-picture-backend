package com.aih.zpicturebackend.model.dto.spaceinvite;

import com.aih.zpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceInviteQueryRequest extends PageRequest implements Serializable {

    private Long spaceId;

    private Long inviteeId;

    private Integer inviteStatus;

    private static final long serialVersionUID = 1L;
}
