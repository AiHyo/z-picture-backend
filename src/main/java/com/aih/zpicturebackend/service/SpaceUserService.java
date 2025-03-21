package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.aih.zpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.aih.zpicturebackend.model.entity.SpaceUser;
import com.aih.zpicturebackend.model.vo.SpaceUserVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author AiH-Madam
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-03-20 19:10:21
*/
public interface SpaceUserService extends IService<SpaceUser> {

    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);


    Wrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    void validSpaceUser(SpaceUser spaceUser, boolean b);
}
