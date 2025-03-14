package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.space.SpaceAddRequest;
import com.aih.zpicturebackend.model.dto.space.SpaceQueryRequest;
import com.aih.zpicturebackend.model.entity.Space;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.SpaceVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author AiH-Madam
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-03-14 15:21:59
 */
public interface SpaceService extends IService<Space> {

    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    void validSpace(Space space, boolean add);

    void fillSpaceBySpaceLevel(Space space);

    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    Wrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    void checkSpaceAuth(User loginUser, Space oldSpace);
}
