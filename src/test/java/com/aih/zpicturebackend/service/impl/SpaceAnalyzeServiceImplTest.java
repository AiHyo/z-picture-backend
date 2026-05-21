package com.aih.zpicturebackend.service.impl;

import com.aih.zpicturebackend.mapper.PictureMapper;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.manage.auth.SpaceUserAuthManager;
import com.aih.zpicturebackend.manage.auth.model.SpaceUserPermissionConstant;
import com.aih.zpicturebackend.model.dto.space.analyze.SpaceUserAnalyzeRequest;
import com.aih.zpicturebackend.model.entity.Space;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.SpaceTypeEnum;
import com.aih.zpicturebackend.model.vo.space.analyze.SpaceUserAnalyzeResponse;
import com.aih.zpicturebackend.service.PictureService;
import com.aih.zpicturebackend.service.SpaceService;
import com.aih.zpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpaceAnalyzeServiceImplTest {

    @Test
    void getSpaceUserAnalyzeDefaultsToDayWhenTimeDimensionIsMissing() {
        SpaceAnalyzeServiceImpl service = new SpaceAnalyzeServiceImpl();
        SpaceService spaceService = mock(SpaceService.class);
        PictureService pictureService = mock(PictureService.class);
        PictureMapper pictureMapper = mock(PictureMapper.class);

        Space space = new Space();
        space.setId(1001L);
        space.setUserId(2001L);
        User loginUser = new User();
        loginUser.setId(2001L);

        when(spaceService.getById(1001L)).thenReturn(space);
        doNothing().when(spaceService).checkSpaceAuth(loginUser, space);
        when(pictureService.getBaseMapper()).thenReturn(pictureMapper);
        when(pictureMapper.selectMaps(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        ReflectionTestUtils.setField(service, "spaceService", spaceService);
        ReflectionTestUtils.setField(service, "pictureService", pictureService);

        SpaceUserAnalyzeRequest request = new SpaceUserAnalyzeRequest();
        request.setSpaceId(1001L);

        List<SpaceUserAnalyzeResponse> result = service.getSpaceUserAnalyze(request, loginUser);

        assertTrue(result.isEmpty());
    }

    @Test
    void getSpaceUserAnalyzeAllowsTeamSpaceAdminMember() {
        SpaceAnalyzeServiceImpl service = new SpaceAnalyzeServiceImpl();
        SpaceService spaceService = mock(SpaceService.class);
        PictureService pictureService = mock(PictureService.class);
        PictureMapper pictureMapper = mock(PictureMapper.class);
        SpaceUserAuthManager spaceUserAuthManager = mock(SpaceUserAuthManager.class);
        UserService userService = mock(UserService.class);

        Space space = new Space();
        space.setId(1001L);
        space.setUserId(3001L);
        space.setSpaceType(SpaceTypeEnum.TEAM.getValue());
        User loginUser = new User();
        loginUser.setId(2001L);

        when(spaceService.getById(1001L)).thenReturn(space);
        doThrow(new BusinessException(ErrorCode.NO_AUTH_ERROR)).when(spaceService).checkSpaceAuth(loginUser, space);
        when(spaceUserAuthManager.getPermissionList(space, loginUser))
                .thenReturn(Collections.singletonList(SpaceUserPermissionConstant.SPACE_USER_MANAGE));
        when(userService.isAdmin(loginUser)).thenReturn(false);
        when(pictureService.getBaseMapper()).thenReturn(pictureMapper);
        when(pictureMapper.selectMaps(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        ReflectionTestUtils.setField(service, "spaceService", spaceService);
        ReflectionTestUtils.setField(service, "pictureService", pictureService);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "spaceUserAuthManager", spaceUserAuthManager);

        SpaceUserAnalyzeRequest request = new SpaceUserAnalyzeRequest();
        request.setSpaceId(1001L);

        List<SpaceUserAnalyzeResponse> result = service.getSpaceUserAnalyze(request, loginUser);

        assertTrue(result.isEmpty());
    }
}
