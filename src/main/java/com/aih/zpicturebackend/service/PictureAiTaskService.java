package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.api.aliYunai.model.CreateOutPaintingTaskResponse;
import com.aih.zpicturebackend.api.aliYunai.model.GetOutPaintingTaskResponse;
import com.aih.zpicturebackend.model.dto.picture.CreatePictureOutPaintingTaskRequest;
import com.aih.zpicturebackend.model.dto.picture.PictureAiTaskQueryRequest;
import com.aih.zpicturebackend.model.entity.PictureAiTask;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.PictureAiTaskVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PictureAiTaskService extends IService<PictureAiTask> {

    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest request, User loginUser);

    GetOutPaintingTaskResponse getPictureOutPaintingTask(String taskId);

    Page<PictureAiTaskVO> listMyPictureAiTaskPage(PictureAiTaskQueryRequest pictureAiTaskQueryRequest, User loginUser);
}
