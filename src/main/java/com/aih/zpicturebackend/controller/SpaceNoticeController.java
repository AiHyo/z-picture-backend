package com.aih.zpicturebackend.controller;

import com.aih.zpicturebackend.common.BaseResponse;
import com.aih.zpicturebackend.common.DeleteRequest;
import com.aih.zpicturebackend.common.ResultUtils;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.exception.ThrowUtils;
import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeAddRequest;
import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeEditRequest;
import com.aih.zpicturebackend.model.dto.spacenotice.SpaceNoticeQueryRequest;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.SpaceNoticeVO;
import com.aih.zpicturebackend.service.SpaceNoticeService;
import com.aih.zpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/spaceNotice")
public class SpaceNoticeController {

    @Resource
    private SpaceNoticeService spaceNoticeService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addSpaceNotice(@RequestBody SpaceNoticeAddRequest spaceNoticeAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long id = spaceNoticeService.addSpaceNotice(spaceNoticeAddRequest, loginUser);
        return ResultUtils.success(id);
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpaceNotice(@RequestBody SpaceNoticeEditRequest spaceNoticeEditRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        spaceNoticeService.editSpaceNotice(spaceNoticeEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpaceNotice(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        spaceNoticeService.deleteSpaceNotice(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/list")
    public BaseResponse<List<SpaceNoticeVO>> listSpaceNotice(@RequestBody SpaceNoticeQueryRequest spaceNoticeQueryRequest,
                                                             HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(spaceNoticeService.listSpaceNoticeVOList(spaceNoticeQueryRequest, loginUser));
    }
}
