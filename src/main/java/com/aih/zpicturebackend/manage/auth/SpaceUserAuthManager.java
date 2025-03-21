package com.aih.zpicturebackend.manage.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aih.zpicturebackend.manage.auth.model.SpaceUserAuthConfig;
import com.aih.zpicturebackend.manage.auth.model.SpaceUserRole;
import com.aih.zpicturebackend.model.entity.Space;
import com.aih.zpicturebackend.model.entity.SpaceUser;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.SpaceRoleEnum;
import com.aih.zpicturebackend.model.enums.SpaceTypeEnum;
import com.aih.zpicturebackend.service.SpaceUserService;
import com.aih.zpicturebackend.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    // 根据角色获取权限
    public List<String> getPermissionsByRole(String spaceUserRole) {
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        // 找到匹配的key[viewer, editor, admin], 返回对应的权限permissions
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> spaceUserRole.equals(r.getKey()))
                .findFirst()
                .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

    // 根据用户和空间，返回对应的权限列表
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限 【所有权限】
        List<String> ADMIN_PERMISSIONS = this.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            // TODO 又看不懂了。。为什么公共图库不是管理员就没权限了。。。
            //  难道因为：这里是为了返回给前端VO的，前端需求只是判断修改和删除权限，就干脆 ADMIN or null
            return new ArrayList<>();
        }
        // 根据其中的 spaceType 判断权限即可
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，通过 loginUserId 和 spaceId => 【spaceUser】
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                }
                // 根据其中的 spaceRole，返回对应权限
                return this.getPermissionsByRole(spaceUser.getSpaceRole());
            default:
                return new ArrayList<>();
        }
    }

}
