package com.aih.zpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.exception.ThrowUtils;
import com.aih.zpicturebackend.mapper.PictureReportMapper;
import com.aih.zpicturebackend.model.dto.picture.PictureReportAddRequest;
import com.aih.zpicturebackend.model.dto.picture.PictureReportProcessRequest;
import com.aih.zpicturebackend.model.dto.picture.PictureReportQueryRequest;
import com.aih.zpicturebackend.model.entity.Picture;
import com.aih.zpicturebackend.model.entity.PictureReport;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.enums.PictureReportStatusEnum;
import com.aih.zpicturebackend.model.enums.PictureReviewStatusEnum;
import com.aih.zpicturebackend.model.vo.PictureReportVO;
import com.aih.zpicturebackend.service.PictureReportService;
import com.aih.zpicturebackend.service.PictureService;
import com.aih.zpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PictureReportServiceImpl extends ServiceImpl<PictureReportMapper, PictureReport>
        implements PictureReportService {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Override
    public long addPictureReport(PictureReportAddRequest pictureReportAddRequest, User loginUser) {
        ThrowUtils.throwIf(pictureReportAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        Long pictureId = pictureReportAddRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(pictureReportAddRequest.getReportReasonType()), ErrorCode.PARAMS_ERROR, "举报类型不能为空");
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(picture.getSpaceId() != null, ErrorCode.OPERATION_ERROR, "仅支持举报公共图库图片");
        boolean exists = this.lambdaQuery()
                .eq(PictureReport::getPictureId, pictureId)
                .eq(PictureReport::getReporterId, loginUser.getId())
                .eq(PictureReport::getReportStatus, PictureReportStatusEnum.PENDING.getValue())
                .exists();
        ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "请勿重复举报");
        PictureReport pictureReport = new PictureReport();
        pictureReport.setPictureId(pictureId);
        pictureReport.setReporterId(loginUser.getId());
        pictureReport.setReportReasonType(pictureReportAddRequest.getReportReasonType());
        pictureReport.setReportReasonText(pictureReportAddRequest.getReportReasonText());
        pictureReport.setReportStatus(PictureReportStatusEnum.PENDING.getValue());
        boolean result = this.save(pictureReport);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return pictureReport.getId();
    }

    @Override
    public void processPictureReport(PictureReportProcessRequest pictureReportProcessRequest, User loginUser) {
        ThrowUtils.throwIf(pictureReportProcessRequest == null || pictureReportProcessRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        PictureReportStatusEnum pictureReportStatusEnum = PictureReportStatusEnum.getEnumByValue(pictureReportProcessRequest.getReportStatus());
        ThrowUtils.throwIf(pictureReportStatusEnum == null || PictureReportStatusEnum.PENDING.equals(pictureReportStatusEnum),
                ErrorCode.PARAMS_ERROR);
        PictureReport pictureReport = this.getById(pictureReportProcessRequest.getId());
        ThrowUtils.throwIf(pictureReport == null, ErrorCode.NOT_FOUND_ERROR, "举报不存在");
        ThrowUtils.throwIf(!ObjUtil.equal(pictureReport.getReportStatus(), PictureReportStatusEnum.PENDING.getValue()),
                ErrorCode.OPERATION_ERROR, "举报已处理");
        pictureReport.setReportStatus(pictureReportStatusEnum.getValue());
        pictureReport.setProcessorId(loginUser.getId());
        pictureReport.setProcessResult(pictureReportProcessRequest.getProcessResult());
        pictureReport.setProcessTime(new Date());
        boolean result = this.updateById(pictureReport);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        if (PictureReportStatusEnum.APPROVED.equals(pictureReportStatusEnum)) {
            Picture picture = new Picture();
            picture.setId(pictureReport.getPictureId());
            picture.setReviewStatus(PictureReviewStatusEnum.REJECT.getValue());
            picture.setReviewMessage(StrUtil.blankToDefault(pictureReportProcessRequest.getProcessResult(), "举报成立，管理员已下架"));
            picture.setReviewerId(loginUser.getId());
            picture.setReviewTime(new Date());
            pictureService.updateById(picture);
        }
    }

    @Override
    public QueryWrapper<PictureReport> getQueryWrapper(PictureReportQueryRequest pictureReportQueryRequest) {
        QueryWrapper<PictureReport> queryWrapper = new QueryWrapper<>();
        if (pictureReportQueryRequest == null) {
            return queryWrapper;
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(pictureReportQueryRequest.getPictureId()), "pictureId", pictureReportQueryRequest.getPictureId());
        queryWrapper.eq(ObjUtil.isNotEmpty(pictureReportQueryRequest.getReportStatus()), "reportStatus", pictureReportQueryRequest.getReportStatus());
        queryWrapper.orderByDesc("createTime");
        return queryWrapper;
    }

    @Override
    public Page<PictureReportVO> getPictureReportVOPage(Page<PictureReport> pictureReportPage) {
        Page<PictureReportVO> pictureReportVOPage = new Page<>(pictureReportPage.getCurrent(), pictureReportPage.getSize(), pictureReportPage.getTotal());
        List<PictureReport> pictureReportList = pictureReportPage.getRecords();
        if (CollUtil.isEmpty(pictureReportList)) {
            return pictureReportVOPage;
        }
        List<PictureReportVO> pictureReportVOList = pictureReportList.stream()
                .map(PictureReportVO::objToVo)
                .collect(Collectors.toList());
        Set<Long> userIdSet = pictureReportList.stream().map(PictureReport::getReporterId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        pictureReportVOList.forEach(pictureReportVO -> {
            List<User> reporterList = userMap.get(pictureReportVO.getReporterId());
            if (CollUtil.isNotEmpty(reporterList)) {
                pictureReportVO.setReporter(userService.getUserVO(reporterList.get(0)));
            }
        });
        pictureReportVOPage.setRecords(pictureReportVOList);
        return pictureReportVOPage;
    }
}
