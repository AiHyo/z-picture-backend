package com.aih.zpicturebackend.model.vo;

import com.aih.zpicturebackend.model.entity.SpaceNotice;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
public class SpaceNoticeVO implements Serializable {

    private Long id;

    private Long spaceId;

    private Long userId;

    private String title;

    private String content;

    private Integer isPinned;

    private Date createTime;

    private Date updateTime;

    private UserVO user;

    private static final long serialVersionUID = 1L;

    public static SpaceNoticeVO objToVo(SpaceNotice spaceNotice) {
        if (spaceNotice == null) {
            return null;
        }
        SpaceNoticeVO spaceNoticeVO = new SpaceNoticeVO();
        BeanUtils.copyProperties(spaceNotice, spaceNoticeVO);
        return spaceNoticeVO;
    }
}
