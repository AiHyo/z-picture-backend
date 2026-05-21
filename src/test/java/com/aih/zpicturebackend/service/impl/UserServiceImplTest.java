package com.aih.zpicturebackend.service.impl;

import com.aih.zpicturebackend.manager.vip.VipCodeStore;
import com.aih.zpicturebackend.mapper.UserMapper;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.SpaceLevelEnum;
import com.aih.zpicturebackend.model.enums.UserRoleEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getMaxAllowedSpaceLevelReturnsCommonForNormalUserWithoutValidVip() {
        UserServiceImpl userService = new UserServiceImpl();
        User user = new User();
        user.setUserRole(UserRoleEnum.USER.getValue());

        assertEquals(SpaceLevelEnum.COMMON.getValue(), userService.getMaxAllowedSpaceLevel(user));
    }

    @Test
    void getMaxAllowedSpaceLevelUsesProfessionalVipLevelFromCodeFile() throws Exception {
        UserServiceImpl userService = userServiceWithCodes("[" +
                "{\"code\":\"VIP-JSON-PRO\",\"used\":true,\"usedUserId\":1001,\"expireTime\":\"2099-12-31 23:59:59\",\"durationDays\":365,\"vipLevel\":1}" +
                "]");
        User user = new User();
        user.setId(1001L);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setVipCode("VIP-JSON-PRO");
        user.setVipExpireTime(futureDate());

        assertEquals(SpaceLevelEnum.PROFESSIONAL.getValue(), userService.getMaxAllowedSpaceLevel(user));
    }

    @Test
    void getMaxAllowedSpaceLevelLoadsVipCodeWhenDefaultUserQueryOmitsIt() throws Exception {
        UserServiceImpl userService = userServiceWithCodes("[" +
                "{\"code\":\"VIP-HIDDEN-CODE\",\"used\":true,\"usedUserId\":1001,\"expireTime\":\"2099-12-31 23:59:59\",\"durationDays\":365,\"vipLevel\":1}" +
                "]");
        User sessionUser = new User();
        sessionUser.setId(1001L);
        sessionUser.setUserRole(UserRoleEnum.USER.getValue());
        sessionUser.setVipExpireTime(futureDate());

        User dbUser = new User();
        dbUser.setId(1001L);
        dbUser.setUserRole(UserRoleEnum.USER.getValue());
        dbUser.setVipCode("VIP-HIDDEN-CODE");
        dbUser.setVipExpireTime(sessionUser.getVipExpireTime());
        UserMapper userMapper = mock(UserMapper.class);
        when(userMapper.selectOne(any())).thenReturn(dbUser);
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);

        assertEquals(SpaceLevelEnum.PROFESSIONAL.getValue(), userService.getMaxAllowedSpaceLevel(sessionUser));
    }

    @Test
    void getMaxAllowedSpaceLevelUsesFlagshipVipLevelFromCodeFile() throws Exception {
        UserServiceImpl userService = userServiceWithCodes("[" +
                "{\"code\":\"VIP-JSON-FLAGSHIP\",\"used\":true,\"usedUserId\":1001,\"expireTime\":\"2099-12-31 23:59:59\",\"durationDays\":365,\"vipLevel\":2}" +
                "]");
        User user = new User();
        user.setId(1001L);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setVipCode("VIP-JSON-FLAGSHIP");
        user.setVipExpireTime(futureDate());

        assertEquals(SpaceLevelEnum.FLAGSHIP.getValue(), userService.getMaxAllowedSpaceLevel(user));
    }

    @Test
    void getMaxAllowedSpaceLevelFallsBackToCommonWhenVipCodeIsNotInCodeFile() throws Exception {
        UserServiceImpl userService = userServiceWithCodes("[]");
        User user = new User();
        user.setId(1001L);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setVipCode("VIP-MISSING");
        user.setVipExpireTime(futureDate());

        assertEquals(SpaceLevelEnum.COMMON.getValue(), userService.getMaxAllowedSpaceLevel(user));
    }

    @Test
    void getMaxAllowedSpaceLevelFallsBackToCommonWhenVipLevelIsInvalid() throws Exception {
        UserServiceImpl userService = userServiceWithCodes("[" +
                "{\"code\":\"VIP-INVALID-LEVEL\",\"used\":true,\"usedUserId\":1001,\"expireTime\":\"2099-12-31 23:59:59\",\"durationDays\":365,\"vipLevel\":99}" +
                "]");
        User user = new User();
        user.setId(1001L);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setVipCode("VIP-INVALID-LEVEL");
        user.setVipExpireTime(futureDate());

        assertEquals(SpaceLevelEnum.COMMON.getValue(), userService.getMaxAllowedSpaceLevel(user));
    }

    @Test
    void getMaxAllowedSpaceLevelFallsBackToCommonWhenVipCodeWasUsedByAnotherUser() throws Exception {
        UserServiceImpl userService = userServiceWithCodes("[" +
                "{\"code\":\"VIP-OTHER-USER\",\"used\":true,\"usedUserId\":2002,\"expireTime\":\"2099-12-31 23:59:59\",\"durationDays\":365,\"vipLevel\":2}" +
                "]");
        User user = new User();
        user.setId(1001L);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setVipCode("VIP-OTHER-USER");
        user.setVipExpireTime(futureDate());

        assertEquals(SpaceLevelEnum.COMMON.getValue(), userService.getMaxAllowedSpaceLevel(user));
    }

    @Test
    void getMaxAllowedSpaceLevelAllowsAdminToCreateAnySpaceLevel() {
        UserServiceImpl userService = new UserServiceImpl();
        User user = new User();
        user.setUserRole(UserRoleEnum.ADMIN.getValue());

        assertEquals(SpaceLevelEnum.FLAGSHIP.getValue(), userService.getMaxAllowedSpaceLevel(user));
    }

    private UserServiceImpl userServiceWithCodes(String json) throws Exception {
        Path codeFile = tempDir.resolve("vip-codes.json");
        Files.write(codeFile, json.getBytes(StandardCharsets.UTF_8));
        UserServiceImpl userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "vipCodeStore", new VipCodeStore(codeFile, objectMapper));
        return userService;
    }

    private Date futureDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }
}
