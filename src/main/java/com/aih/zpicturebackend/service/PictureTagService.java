package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.picture.PictureTagQueryRequest;
import com.aih.zpicturebackend.model.entity.PictureTag;
import com.aih.zpicturebackend.model.vo.PictureTagVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PictureTagService extends IService<PictureTag> {

    void validTagName(String tagName);

    QueryWrapper<PictureTag> getQueryWrapper(PictureTagQueryRequest pictureTagQueryRequest);

    List<PictureTagVO> getPictureTagVOList(List<PictureTag> pictureTagList);

    List<String> listTagNameList();
}
