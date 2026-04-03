package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.spaceinvite.SpaceInviteAddRequest;
import com.aih.zpicturebackend.model.dto.spaceinvite.SpaceInviteQueryRequest;
import com.aih.zpicturebackend.model.entity.SpaceInvite;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.SpaceInviteVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SpaceInviteService extends IService<SpaceInvite> {

    long addSpaceInvite(SpaceInviteAddRequest spaceInviteAddRequest, User loginUser);

    void acceptSpaceInvite(Long id, User loginUser);

    void rejectSpaceInvite(Long id, User loginUser);

    QueryWrapper<SpaceInvite> getQueryWrapper(SpaceInviteQueryRequest spaceInviteQueryRequest);

    Page<SpaceInviteVO> getSpaceInviteVOPage(Page<SpaceInvite> spaceInvitePage);
}
