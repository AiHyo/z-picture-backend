package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.picture.PictureReportAddRequest;
import com.aih.zpicturebackend.model.dto.picture.PictureReportProcessRequest;
import com.aih.zpicturebackend.model.dto.picture.PictureReportQueryRequest;
import com.aih.zpicturebackend.model.entity.PictureReport;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.PictureReportVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PictureReportService extends IService<PictureReport> {

    long addPictureReport(PictureReportAddRequest pictureReportAddRequest, User loginUser);

    void processPictureReport(PictureReportProcessRequest pictureReportProcessRequest, User loginUser);

    QueryWrapper<PictureReport> getQueryWrapper(PictureReportQueryRequest pictureReportQueryRequest);

    Page<PictureReportVO> getPictureReportVOPage(Page<PictureReport> pictureReportPage);
}
