package com.aih.zpicturebackend.service.impl;

import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.model.dto.space.SpaceAddRequest;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.SpaceLevelEnum;
import com.aih.zpicturebackend.model.enums.SpaceTypeEnum;
import com.aih.zpicturebackend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpaceServiceImplTest {

    @Test
    void addSpaceRejectsSpaceLevelAboveUserMaxAllowedLevel() {
        SpaceServiceImpl spaceService = new SpaceServiceImpl();
        UserService userService = mock(UserService.class);
        ReflectionTestUtils.setField(spaceService, "userService", userService);

        User user = new User();
        user.setId(1001L);
        SpaceAddRequest request = new SpaceAddRequest();
        request.setSpaceName("旗舰团队");
        request.setSpaceType(SpaceTypeEnum.TEAM.getValue());
        request.setSpaceLevel(SpaceLevelEnum.FLAGSHIP.getValue());
        when(userService.getMaxAllowedSpaceLevel(user)).thenReturn(SpaceLevelEnum.PROFESSIONAL.getValue());

        BusinessException exception = assertThrows(BusinessException.class, () -> spaceService.addSpace(request, user));

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }
}
