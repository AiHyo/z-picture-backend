package com.aih.zpicturebackend.manage.websocket;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aih.zpicturebackend.manage.auth.SpaceUserAuthManager;
import com.aih.zpicturebackend.manage.auth.model.SpaceUserPermissionConstant;
import com.aih.zpicturebackend.model.entity.Picture;
import com.aih.zpicturebackend.model.entity.Space;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.SpaceTypeEnum;
import com.aih.zpicturebackend.service.PictureService;
import com.aih.zpicturebackend.service.SpaceService;
import com.aih.zpicturebackend.service.UserService;
import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * WebSocket握手拦截器。实现 HandshakeInterceptor 接口，用于在握手之前和之后执行自定义逻辑。
 */
@Component
@Slf4j
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, @NotNull Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            // 获取 httpServletRequest
            HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 获取请求参数
            String pictureId = httpServletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.error("缺少图片参数，拒绝握手");
                return false;
            }
            User loginUser = userService.getLoginUser(httpServletRequest);
            if (ObjUtil.isEmpty(loginUser)) {
                log.error("用户未登录，拒绝握手");
                return false;
            }
            // 校验用户是否有该图片的权限
            Picture picture = pictureService.getById(pictureId);
            if (picture == null) {
                log.error("图片不存在，拒绝握手");
                return false;
            }
            Long spaceId = picture.getSpaceId();
            Space space = null;
            if (spaceId != null) {
                space = spaceService.getById(spaceId);
                if (space == null) {
                    log.error("空间不存在，拒绝握手");
                    return false;
                }
                if (space.getSpaceType() != SpaceTypeEnum.TEAM.getValue()) {
                    log.info("不是团队空间，拒绝握手");
                    return false;
                }
            }
            List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
            if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                log.error("没有图片编辑权限，拒绝握手");
                return false;
            }
            // Spring WebSocket框架在内部维护这个attributes Map
            // 在握手成功后，框架将这个Map绑定到新创建的WebSocketSession对象
            // 当Handler的方法被调用时，这个包含属性的session对象会被传入
            // 所以，我们就可以在Handler中访问这些属性：session.getAttributes().get("user")
            attributes.put("user", loginUser);
            attributes.put("userId", loginUser.getId());
            attributes.put("pictureId", Long.valueOf(pictureId)); // 来源于 httpServletRequest.getParameter("pictureId") 需要转换为 Long 类型
            return true;
        }
        return true;
    }

    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response, @NotNull WebSocketHandler wsHandler, Exception exception) {
    }
}
