package com.aih.zpicturebackend.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aih.zpicturebackend.api.aliYunai.AliYunAiApi;
import com.aih.zpicturebackend.api.aliYunai.model.CreateOutPaintingTaskRequest;
import com.aih.zpicturebackend.api.aliYunai.model.CreateOutPaintingTaskResponse;
import com.aih.zpicturebackend.api.aliYunai.model.GetOutPaintingTaskResponse;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.exception.ThrowUtils;
import com.aih.zpicturebackend.mapper.PictureAiTaskMapper;
import com.aih.zpicturebackend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.aih.zpicturebackend.model.dto.picture.PictureAiTaskQueryRequest;
import com.aih.zpicturebackend.model.entity.Picture;
import com.aih.zpicturebackend.model.entity.PictureAiTask;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.PictureAiTaskStatusEnum;
import com.aih.zpicturebackend.model.vo.PictureAiTaskVO;
import com.aih.zpicturebackend.service.PictureAiTaskService;
import com.aih.zpicturebackend.service.PictureService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PictureAiTaskServiceImpl extends ServiceImpl<PictureAiTaskMapper, PictureAiTask>
        implements PictureAiTaskService {

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private PictureService pictureService;

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null || request.getPictureId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Picture picture = pictureService.getById(request.getPictureId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        taskRequest.setInput(input);
        BeanUtils.copyProperties(request, taskRequest);
        CreateOutPaintingTaskResponse response = aliYunAiApi.createOutPaintingTask(taskRequest);
        String taskId = response == null || response.getOutput() == null ? null : response.getOutput().getTaskId();
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.OPERATION_ERROR, "创建 AI 任务失败");
        PictureAiTask pictureAiTask = new PictureAiTask();
        pictureAiTask.setPictureId(picture.getId());
        pictureAiTask.setUserId(loginUser.getId());
        pictureAiTask.setTaskType("out_painting");
        pictureAiTask.setExternalTaskId(taskId);
        pictureAiTask.setTaskStatus(PictureAiTaskStatusEnum.RUNNING.getValue());
        pictureAiTask.setRequestParams(JSONUtil.toJsonStr(request));
        boolean result = this.save(pictureAiTask);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return response;
    }

    @Override
    public GetOutPaintingTaskResponse getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse response = aliYunAiApi.getOutPaintingTask(taskId);
        if (response == null || response.getOutput() == null) {
            return response;
        }
        PictureAiTask pictureAiTask = this.lambdaQuery()
                .eq(PictureAiTask::getExternalTaskId, taskId)
                .one();
        if (pictureAiTask == null) {
            return response;
        }
        String taskStatus = response.getOutput().getTaskStatus();
        if ("SUCCEEDED".equals(taskStatus)) {
            pictureAiTask.setTaskStatus(PictureAiTaskStatusEnum.SUCCESS.getValue());
            pictureAiTask.setResultUrl(response.getOutput().getOutputImageUrl());
            pictureAiTask.setFinishTime(new Date());
            pictureAiTask.setErrorMessage(null);
        } else if ("FAILED".equals(taskStatus)) {
            pictureAiTask.setTaskStatus(PictureAiTaskStatusEnum.FAILED.getValue());
            pictureAiTask.setErrorMessage(response.getOutput().getMessage());
            pictureAiTask.setFinishTime(new Date());
        } else if (ObjUtil.notEqual(pictureAiTask.getTaskStatus(), PictureAiTaskStatusEnum.SUCCESS.getValue())) {
            pictureAiTask.setTaskStatus(PictureAiTaskStatusEnum.RUNNING.getValue());
        }
        this.updateById(pictureAiTask);
        return response;
    }

    @Override
    public Page<PictureAiTaskVO> listMyPictureAiTaskPage(PictureAiTaskQueryRequest pictureAiTaskQueryRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        PictureAiTaskQueryRequest queryRequest = pictureAiTaskQueryRequest == null ? new PictureAiTaskQueryRequest() : pictureAiTaskQueryRequest;
        Page<PictureAiTask> pictureAiTaskPage = this.lambdaQuery()
                .eq(PictureAiTask::getUserId, loginUser.getId())
                .eq(queryRequest.getTaskStatus() != null, PictureAiTask::getTaskStatus, queryRequest.getTaskStatus())
                .orderByDesc(PictureAiTask::getCreateTime)
                .page(new Page<>(queryRequest.getCurrent(), queryRequest.getPageSize()));
        Page<PictureAiTaskVO> pictureAiTaskVOPage = new Page<>(pictureAiTaskPage.getCurrent(), pictureAiTaskPage.getSize(), pictureAiTaskPage.getTotal());
        List<PictureAiTaskVO> pictureAiTaskVOList = pictureAiTaskPage.getRecords().stream()
                .map(PictureAiTaskVO::objToVo)
                .collect(Collectors.toList());
        pictureAiTaskVOPage.setRecords(pictureAiTaskVOList);
        return pictureAiTaskVOPage;
    }
}
