package com.aih.zpicturebackend.controller;

import com.aih.zpicturebackend.common.BaseResponse;
import com.aih.zpicturebackend.common.ResultUtils;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.exception.ThrowUtils;
import com.aih.zpicturebackend.manage.auth.annotation.SaSpaceCheckPermission;
import com.aih.zpicturebackend.manage.auth.model.SpaceUserPermissionConstant;
import com.aih.zpicturebackend.model.dto.spaceinvite.SpaceInviteAddRequest;
import com.aih.zpicturebackend.model.dto.spaceinvite.SpaceInviteHandleRequest;
import com.aih.zpicturebackend.model.dto.spaceinvite.SpaceInviteQueryRequest;
import com.aih.zpicturebackend.model.entity.SpaceInvite;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.SpaceInviteVO;
import com.aih.zpicturebackend.service.SpaceInviteService;
import com.aih.zpicturebackend.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/spaceInvite")
public class SpaceInviteController {

    @Resource
    private SpaceInviteService spaceInviteService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceInvite(@RequestBody SpaceInviteAddRequest spaceInviteAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long id = spaceInviteService.addSpaceInvite(spaceInviteAddRequest, loginUser);
        return ResultUtils.success(id);
    }

    @PostMapping("/accept")
    public BaseResponse<Boolean> acceptSpaceInvite(@RequestBody SpaceInviteHandleRequest spaceInviteHandleRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceInviteHandleRequest == null || spaceInviteHandleRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        spaceInviteService.acceptSpaceInvite(spaceInviteHandleRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/reject")
    public BaseResponse<Boolean> rejectSpaceInvite(@RequestBody SpaceInviteHandleRequest spaceInviteHandleRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceInviteHandleRequest == null || spaceInviteHandleRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        spaceInviteService.rejectSpaceInvite(spaceInviteHandleRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/list/page")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Page<SpaceInviteVO>> listSpaceInviteByPage(@RequestBody SpaceInviteQueryRequest spaceInviteQueryRequest) {
        ThrowUtils.throwIf(spaceInviteQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<SpaceInvite> spaceInvitePage = spaceInviteService.page(
                new Page<>(spaceInviteQueryRequest.getCurrent(), spaceInviteQueryRequest.getPageSize()),
                spaceInviteService.getQueryWrapper(spaceInviteQueryRequest)
        );
        return ResultUtils.success(spaceInviteService.getSpaceInviteVOPage(spaceInvitePage));
    }

    @PostMapping("/list/my")
    public BaseResponse<Page<SpaceInviteVO>> listMySpaceInviteByPage(@RequestBody(required = false) SpaceInviteQueryRequest spaceInviteQueryRequest,
                                                                     HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceInviteQueryRequest queryRequest = spaceInviteQueryRequest == null ? new SpaceInviteQueryRequest() : spaceInviteQueryRequest;
        queryRequest.setInviteeId(loginUser.getId());
        Page<SpaceInvite> spaceInvitePage = spaceInviteService.page(
                new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize()),
                spaceInviteService.getQueryWrapper(queryRequest)
        );
        return ResultUtils.success(spaceInviteService.getSpaceInviteVOPage(spaceInvitePage));
    }
}
