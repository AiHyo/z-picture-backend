package com.aih.zpicturebackend.manage.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.manage.auth.model.SpaceUserPermissionConstant;
import com.aih.zpicturebackend.model.entity.Picture;
import com.aih.zpicturebackend.model.entity.Space;
import com.aih.zpicturebackend.model.entity.SpaceUser;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.SpaceRoleEnum;
import com.aih.zpicturebackend.model.enums.SpaceTypeEnum;
import com.aih.zpicturebackend.service.PictureService;
import com.aih.zpicturebackend.service.SpaceService;
import com.aih.zpicturebackend.service.SpaceUserService;
import com.aih.zpicturebackend.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.aih.zpicturebackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface { // 实现 StpInterface 接口
    // 默认是 /api
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private PictureService pictureService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断 loginType，仅对类型为 "space" 进行权限校验
        // if ( !"space".equals(loginType) ) {
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return new ArrayList<>();
        }
        // 管理员权限 【所有权限】
        List<String> ADMIN_PERMISSIONS = spaceUserAuthManager.getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 获取上下文对象
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        // 如果请求中所有字段都为空，表示查询公共图库，可以通过
        if (this.isAllFieldsNull(authContext)){
            return ADMIN_PERMISSIONS;
        }
        // 根据 入参loginId 从sa-token的session中获取登陆用户信息
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户未登录");
        }
        Long loginUserId = loginUser.getId(); // 获取登陆用户的userId后面用得上
        // 尝试从上下文【直接】获取 【SpaceUser】 对象，可以直接根据其中的spaceRole返回对应权限
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
        }
        // 尝试从上下文通过 spaceUserId => 【SpaceUser】 对象
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            spaceUser = spaceUserService.getById(spaceUserId);
            if (spaceUser == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间用户信息");
            }
            // TODO 没弄懂。。。这里在干嘛。。。可能是因为👇
            // 因为根据spaceUserId找到的团队空间，通过查询判断当前登陆用户是否属于该空间的
            // 并根据查询出的结果其中的spaceRole返回对应权限
            SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                    .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                    .eq(SpaceUser::getUserId, loginUserId)
                    .one();
            if (loginSpaceUser == null) {
                return new ArrayList<>();
            }
            // 这里会导致管理员在私有空间没有权限，可以再查一次库处理
            return spaceUserAuthManager.getPermissionsByRole(loginSpaceUser.getSpaceRole());
        }
        // 尝试从上下文通过 spaceId => 【Space】 对象
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            // 继续尝试从上下文通过 pictureId => Picture => spaceId (为了后续通过 spaceId => 【Space】 对象)
            Long pictureId = authContext.getPictureId();
            // TODO 为啥没参数反而直接通过所有权限。。 可能是因为👇
            // 图片 id 也没有，则只是查看公共图库，不会对系统[space、picture]造成破坏，这里直接通过
            if (pictureId == null) {
                return ADMIN_PERMISSIONS;
            }
            Picture picture = pictureService.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                    .one();
            if (picture == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到图片信息");
            }
            spaceId = picture.getSpaceId();
            // 公共图库，仅本人或管理员可操作
            if (spaceId == null) {
                if (picture.getUserId().equals(loginUserId) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    // 否则，只可查看
                    return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
                }
            }
        }
        // 有了 spaceId 就获取 【Space】 即可
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到空间信息");
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
                spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                }
                // 根据其中的 spaceRole，返回对应权限
                return spaceUserAuthManager.getPermissionsByRole(spaceUser.getSpaceRole());
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 本项目中不使用。返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        SpaceUserAuthContext authRequest = null;
        // 获取请求对象
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String header = request.getHeader(Header.CONTENT_TYPE.getValue());
            // 兼容 get 和 post 请求
            if (ContentType.JSON.getValue().equals(header)) {
                // HttpServletRequest 的 body 值是个流，只支持读取一次，读完就没了！
                // 所以需要在 config 包下自定义请求包装类和请求包装类过滤器。
                String body = ServletUtil.getBody(request);
                authRequest = JSONUtil.toBean(body, SpaceUserAuthContext.class);
            } else {
                Map<String, String[]> parameterMap = request.getParameterMap();
                authRequest = BeanUtil.toBean(parameterMap, SpaceUserAuthContext.class);
            }
            // 根据请求路径 区分 id 字段含义
            Long id = authRequest.getId();
            if (ObjUtil.isNotNull(id)) {
                String requestUri = request.getRequestURI();
                String partUri = requestUri.replace(contextPath + "/", "");
                String moduleName = StrUtil.subBefore(partUri, "/", false);
                switch (moduleName) {
                    case "picture":
                        authRequest.setPictureId(id);
                        break;
                    case "spaceUser":
                        authRequest.setSpaceUserId(id);
                        break;
                    case "space":
                        authRequest.setSpaceId(id);
                        break;
                    default:
                }
            }


        }
        return authRequest;
    }

    /**
     * 判断对象的所有字段是否为空
     *
     * @param object
     * @return
     */
    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }
}
