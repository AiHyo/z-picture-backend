package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.picture.*;
import com.aih.zpicturebackend.model.entity.Picture;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author AiH-Madam
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-03-11 10:48:27
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取查询的 queryWrapper
     * @param pictureQueryRequest 图片请求类
     * @return 可用来查询的 queryWrapper
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);


    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 获取单个图片的 VO 对象
     * @param picture picture 对象
     * @param request request 请求
     * @return 对应图片的 VO
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 成功创建的图片数
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    @Transactional(rollbackFor = Exception.class)
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    void deletePicture(long pictureId, User loginUser);

    @Async
    void clearPictureFile(Picture oldPicture);

    void checkPictureAuth(User loginUser, Picture picture);

    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);
}
