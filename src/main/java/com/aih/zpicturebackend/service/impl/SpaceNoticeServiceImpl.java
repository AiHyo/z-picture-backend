package com.aih.zpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.exception.ThrowUtils;
import com.aih.zpicturebackend.manage.auth.SpaceUserAuthManager;
import com.aih.zpicturebackend.manage.auth.model.SpaceUserPermissionConstant;
import com.aih.zpicturebackend.mapper.SpaceNoticeMapper;
import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeAddRequest;
import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeEditRequest;
import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeQueryRequest;
import com.aih.zpicturebackend.model.entity.Space;
import com.aih.zpicturebackend.model.entity.SpaceNotice;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.SpaceTypeEnum;
import com.aih.zpicturebackend.model.vo.SpaceNoticeVO;
import com.aih.zpicturebackend.service.SpaceNoticeService;
import com.aih.zpicturebackend.service.SpaceService;
import com.aih.zpicturebackend.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpaceNoticeServiceImpl extends ServiceImpl<SpaceNoticeMapper, SpaceNotice>
        implements SpaceNoticeService {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public long addSpaceNotice(SpaceNoticeAddRequest spaceNoticeAddRequest, User loginUser) {
        ThrowUtils.throwIf(spaceNoticeAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Space space = getTeamSpace(spaceNoticeAddRequest.getSpaceId());
        ensureManageAuth(space, loginUser);
        validNotice(spaceNoticeAddRequest.getTitle(), spaceNoticeAddRequest.getContent());
        SpaceNotice spaceNotice = new SpaceNotice();
        spaceNotice.setSpaceId(space.getId());
        spaceNotice.setUserId(loginUser.getId());
        spaceNotice.setTitle(spaceNoticeAddRequest.getTitle());
        spaceNotice.setContent(spaceNoticeAddRequest.getContent());
        spaceNotice.setIsPinned(ObjUtil.defaultIfNull(spaceNoticeAddRequest.getIsPinned(), 0));
        boolean result = this.save(spaceNotice);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceNotice.getId();
    }

    @Override
    public void editSpaceNotice(SpaceNoticeEditRequest spaceNoticeEditRequest, User loginUser) {
        ThrowUtils.throwIf(spaceNoticeEditRequest == null || spaceNoticeEditRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        SpaceNotice dbSpaceNotice = this.getById(spaceNoticeEditRequest.getId());
        ThrowUtils.throwIf(dbSpaceNotice == null, ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        ThrowUtils.throwIf(spaceNoticeEditRequest.getSpaceId() != null
                && !ObjUtil.equal(spaceNoticeEditRequest.getSpaceId(), dbSpaceNotice.getSpaceId()), ErrorCode.PARAMS_ERROR);
        Space space = getTeamSpace(dbSpaceNotice.getSpaceId());
        ensureManageAuth(space, loginUser);
        validNotice(spaceNoticeEditRequest.getTitle(), spaceNoticeEditRequest.getContent());
        SpaceNotice spaceNotice = new SpaceNotice();
        spaceNotice.setId(dbSpaceNotice.getId());
        spaceNotice.setTitle(spaceNoticeEditRequest.getTitle());
        spaceNotice.setContent(spaceNoticeEditRequest.getContent());
        spaceNotice.setIsPinned(ObjUtil.defaultIfNull(spaceNoticeEditRequest.getIsPinned(), 0));
        boolean result = this.updateById(spaceNotice);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void deleteSpaceNotice(Long id, User loginUser) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        SpaceNotice dbSpaceNotice = this.getById(id);
        ThrowUtils.throwIf(dbSpaceNotice == null, ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        Space space = getTeamSpace(dbSpaceNotice.getSpaceId());
        ensureManageAuth(space, loginUser);
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public List<SpaceNoticeVO> listSpaceNoticeVOList(SpaceNoticeQueryRequest spaceNoticeQueryRequest, User loginUser) {
        ThrowUtils.throwIf(spaceNoticeQueryRequest == null || spaceNoticeQueryRequest.getSpaceId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Space space = getTeamSpace(spaceNoticeQueryRequest.getSpaceId());
        ensureViewAuth(space, loginUser);
        List<SpaceNotice> spaceNoticeList = this.lambdaQuery()
                .eq(SpaceNotice::getSpaceId, space.getId())
                .orderByDesc(SpaceNotice::getIsPinned)
                .orderByDesc(SpaceNotice::getCreateTime)
                .list();
        if (CollUtil.isEmpty(spaceNoticeList)) {
            return java.util.Collections.emptyList();
        }
        List<SpaceNoticeVO> spaceNoticeVOList = spaceNoticeList.stream()
                .map(SpaceNoticeVO::objToVo)
                .collect(Collectors.toList());
        Set<Long> userIdSet = spaceNoticeList.stream().map(SpaceNotice::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        spaceNoticeVOList.forEach(spaceNoticeVO -> {
            List<User> userList = userMap.get(spaceNoticeVO.getUserId());
            if (CollUtil.isNotEmpty(userList)) {
                spaceNoticeVO.setUser(userService.getUserVO(userList.get(0)));
            }
        });
        return spaceNoticeVOList;
    }

    private Space getTeamSpace(Long spaceId) {
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!SpaceTypeEnum.TEAM.equals(SpaceTypeEnum.getEnumByValue(space.getSpaceType())),
                ErrorCode.OPERATION_ERROR, "只有团队空间支持公告");
        return space;
    }

    private void ensureManageAuth(Space space, User loginUser) {
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        ThrowUtils.throwIf(!permissionList.contains(SpaceUserPermissionConstant.SPACE_USER_MANAGE), ErrorCode.NO_AUTH_ERROR);
    }

    private void ensureViewAuth(Space space, User loginUser) {
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        ThrowUtils.throwIf(!permissionList.contains(SpaceUserPermissionConstant.PICTURE_VIEW)
                && !permissionList.contains(SpaceUserPermissionConstant.SPACE_USER_MANAGE), ErrorCode.NO_AUTH_ERROR);
    }

    private void validNotice(String title, String content) {
        if (StrUtil.isBlank(title) || StrUtil.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告标题或内容不能为空");
        }
        if (title.length() > 60) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告标题过长");
        }
        if (content.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告内容过长");
        }
    }
}
