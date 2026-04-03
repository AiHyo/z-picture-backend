package com.aih.zpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.exception.ThrowUtils;
import com.aih.zpicturebackend.mapper.SpaceInviteMapper;
import com.aih.zpicturebackend.model.dto.spaceinvite.SpaceInviteAddRequest;
import com.aih.zpicturebackend.model.dto.spaceinvite.SpaceInviteQueryRequest;
import com.aih.zpicturebackend.model.entity.Space;
import com.aih.zpicturebackend.model.entity.SpaceInvite;
import com.aih.zpicturebackend.model.entity.SpaceUser;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.SpaceInviteStatusEnum;
import com.aih.zpicturebackend.model.enums.SpaceRoleEnum;
import com.aih.zpicturebackend.model.enums.SpaceTypeEnum;
import com.aih.zpicturebackend.model.vo.SpaceInviteVO;
import com.aih.zpicturebackend.service.SpaceInviteService;
import com.aih.zpicturebackend.service.SpaceService;
import com.aih.zpicturebackend.service.SpaceUserService;
import com.aih.zpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpaceInviteServiceImpl extends ServiceImpl<SpaceInviteMapper, SpaceInvite>
        implements SpaceInviteService {

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    @Override
    public long addSpaceInvite(SpaceInviteAddRequest spaceInviteAddRequest, User loginUser) {
        ThrowUtils.throwIf(spaceInviteAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Long spaceId = spaceInviteAddRequest.getSpaceId();
        Long inviteeId = spaceInviteAddRequest.getInviteeId();
        ThrowUtils.throwIf(spaceId == null || inviteeId == null, ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        ThrowUtils.throwIf(!SpaceTypeEnum.TEAM.equals(SpaceTypeEnum.getEnumByValue(space.getSpaceType())),
                ErrorCode.OPERATION_ERROR, "只有团队空间支持邀请");
        User invitee = userService.getById(inviteeId);
        ThrowUtils.throwIf(invitee == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(ObjUtil.equal(inviteeId, loginUser.getId()), ErrorCode.PARAMS_ERROR, "不能邀请自己");
        ensureManageAuth(space, loginUser);
        boolean joined = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, inviteeId)
                .exists();
        ThrowUtils.throwIf(joined, ErrorCode.OPERATION_ERROR, "用户已加入该空间");
        boolean pending = this.lambdaQuery()
                .eq(SpaceInvite::getSpaceId, spaceId)
                .eq(SpaceInvite::getInviteeId, inviteeId)
                .eq(SpaceInvite::getInviteStatus, SpaceInviteStatusEnum.PENDING.getValue())
                .exists();
        ThrowUtils.throwIf(pending, ErrorCode.OPERATION_ERROR, "已有待处理邀请");
        String spaceRole = spaceInviteAddRequest.getSpaceRole();
        if (StrUtil.isBlank(spaceRole)) {
            spaceRole = SpaceRoleEnum.VIEWER.getValue();
        }
        ThrowUtils.throwIf(SpaceRoleEnum.getEnumByValue(spaceRole) == null, ErrorCode.PARAMS_ERROR, "空间角色错误");
        SpaceInvite spaceInvite = new SpaceInvite();
        spaceInvite.setSpaceId(spaceId);
        spaceInvite.setInviterId(loginUser.getId());
        spaceInvite.setInviteeId(inviteeId);
        spaceInvite.setSpaceRole(spaceRole);
        spaceInvite.setInviteMessage(spaceInviteAddRequest.getInviteMessage());
        spaceInvite.setInviteStatus(SpaceInviteStatusEnum.PENDING.getValue());
        boolean result = this.save(spaceInvite);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceInvite.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptSpaceInvite(Long id, User loginUser) {
        SpaceInvite spaceInvite = getPendingInvite(id, loginUser);
        boolean joined = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceInvite.getSpaceId())
                .eq(SpaceUser::getUserId, loginUser.getId())
                .exists();
        ThrowUtils.throwIf(joined, ErrorCode.OPERATION_ERROR, "用户已加入该空间");
        spaceInvite.setInviteStatus(SpaceInviteStatusEnum.ACCEPTED.getValue());
        spaceInvite.setHandleTime(new Date());
        boolean updateResult = this.updateById(spaceInvite);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceInvite.getSpaceId());
        spaceUser.setUserId(loginUser.getId());
        spaceUser.setSpaceRole(spaceInvite.getSpaceRole());
        boolean saveResult = spaceUserService.save(spaceUser);
        ThrowUtils.throwIf(!saveResult, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void rejectSpaceInvite(Long id, User loginUser) {
        SpaceInvite spaceInvite = getPendingInvite(id, loginUser);
        spaceInvite.setInviteStatus(SpaceInviteStatusEnum.REJECTED.getValue());
        spaceInvite.setHandleTime(new Date());
        boolean result = this.updateById(spaceInvite);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public QueryWrapper<SpaceInvite> getQueryWrapper(SpaceInviteQueryRequest spaceInviteQueryRequest) {
        QueryWrapper<SpaceInvite> queryWrapper = new QueryWrapper<>();
        if (spaceInviteQueryRequest == null) {
            return queryWrapper;
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceInviteQueryRequest.getSpaceId()), "spaceId", spaceInviteQueryRequest.getSpaceId());
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceInviteQueryRequest.getInviteeId()), "inviteeId", spaceInviteQueryRequest.getInviteeId());
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceInviteQueryRequest.getInviteStatus()), "inviteStatus", spaceInviteQueryRequest.getInviteStatus());
        queryWrapper.orderByDesc("createTime");
        return queryWrapper;
    }

    @Override
    public Page<SpaceInviteVO> getSpaceInviteVOPage(Page<SpaceInvite> spaceInvitePage) {
        Page<SpaceInviteVO> spaceInviteVOPage = new Page<>(spaceInvitePage.getCurrent(), spaceInvitePage.getSize(), spaceInvitePage.getTotal());
        List<SpaceInvite> records = spaceInvitePage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return spaceInviteVOPage;
        }
        List<SpaceInviteVO> spaceInviteVOList = records.stream()
                .map(SpaceInviteVO::objToVo)
                .collect(Collectors.toList());
        Set<Long> userIdSet = records.stream()
                .flatMap(spaceInvite -> java.util.stream.Stream.of(spaceInvite.getInviterId(), spaceInvite.getInviteeId()))
                .collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        spaceInviteVOList.forEach(spaceInviteVO -> {
            List<User> inviterList = userMap.get(spaceInviteVO.getInviterId());
            List<User> inviteeList = userMap.get(spaceInviteVO.getInviteeId());
            if (CollUtil.isNotEmpty(inviterList)) {
                spaceInviteVO.setInviter(userService.getUserVO(inviterList.get(0)));
            }
            if (CollUtil.isNotEmpty(inviteeList)) {
                spaceInviteVO.setInvitee(userService.getUserVO(inviteeList.get(0)));
            }
        });
        spaceInviteVOPage.setRecords(spaceInviteVOList);
        return spaceInviteVOPage;
    }

    private void ensureManageAuth(Space space, User loginUser) {
        if (userService.isAdmin(loginUser) || ObjUtil.equal(space.getUserId(), loginUser.getId())) {
            return;
        }
        SpaceUser spaceUser = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getSpaceId, space.getId())
                .eq(SpaceUser::getUserId, loginUser.getId())
                .one();
        ThrowUtils.throwIf(spaceUser == null || !SpaceRoleEnum.ADMIN.getValue().equals(spaceUser.getSpaceRole()),
                ErrorCode.NO_AUTH_ERROR);
    }

    private SpaceInvite getPendingInvite(Long id, User loginUser) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        SpaceInvite spaceInvite = this.getById(id);
        ThrowUtils.throwIf(spaceInvite == null, ErrorCode.NOT_FOUND_ERROR, "邀请不存在");
        ThrowUtils.throwIf(!ObjUtil.equal(spaceInvite.getInviteeId(), loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(!ObjUtil.equal(spaceInvite.getInviteStatus(), SpaceInviteStatusEnum.PENDING.getValue()),
                ErrorCode.OPERATION_ERROR, "邀请已处理");
        return spaceInvite;
    }
}
