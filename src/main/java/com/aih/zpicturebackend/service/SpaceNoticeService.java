package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeAddRequest;
import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeEditRequest;
import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeQueryRequest;
import com.aih.zpicturebackend.model.entity.SpaceNotice;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.SpaceNoticeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SpaceNoticeService extends IService<SpaceNotice> {

    long addSpaceNotice(SpaceNoticeAddRequest spaceNoticeAddRequest, User loginUser);

    void editSpaceNotice(SpaceNoticeEditRequest spaceNoticeEditRequest, User loginUser);

    void deleteSpaceNotice(Long id, User loginUser);

    List<SpaceNoticeVO> listSpaceNoticeVOList(SpaceNoticeQueryRequest spaceNoticeQueryRequest, User loginUser);
}
