package com.aih.zpicturebackend.manage.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.aih.zpicturebackend.config.CosClientConfig;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.manage.CosManager;
import com.aih.zpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.CIUploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    /**
     * 模板方法，定义上传流程
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 校验图片
        validPicture(inputSource);
        // 2. 图片上传地址
        // 获取原文件名
        String originFilename = this.getOriginFilename(inputSource);
        // 自定义文件上传路径【/前缀/日期_16位uuid.后缀】,而不是使用原始文件名称，可以增加安全性
        String uploadFilename = DateUtil.formatDate(new Date()) + "_" + RandomUtil.randomString(16) + "." + FileUtil.getSuffix(originFilename);
        String uploadPath = uploadPathPrefix + "/" + uploadFilename;
        File file = null;
        try {
            // 3. 创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源（本地或 URL）
            this.processFile(inputSource, file);
            // 4. 上传图片并进行对应规则处理到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 上传结果中获取数据万象(CI, Cloud Infinite)处理的结果信息。
            CIUploadResult ciUploadResult = putObjectResult.getCiUploadResult();
            // 基础信息
            ImageInfo imageInfo = ciUploadResult.getOriginalInfo().getImageInfo();
            // 获取图片处理操作的结果集 0：压缩图 1：缩略图
            List<CIObject> objectList = ciUploadResult.getProcessResults().getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressedCiObject = objectList.get(0); // 压缩图
                CIObject thumbnailCiObject = compressedCiObject; // 默认缩略图为压缩图【因为图片过小,则没有进行缩略处理】
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                return this.buildResult(originFilename, compressedCiObject, thumbnailCiObject, imageInfo);
            }
            // 5. 封装返回结果
            return this.buildResult(originFilename, file, uploadPath, imageInfo);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 6. 清理临时文件
            deleteTempFile(file);
        }
    }

    /**
     * 封装返回结果
     *
     * @param originFilename
     * @param compressedCiObject 压缩图对象
     * @param thumbnailCiObject  缩略图对象
     * @return
     */
    private UploadPictureResult buildResult(String originFilename, CIObject compressedCiObject, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        // 缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        // 压缩图地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 封装返回结果
     *
     * @param originFilename
     * @param file
     * @param uploadPath
     * @param imageInfo      对象存储返回的图片信息
     */
    private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }


    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;


    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}
