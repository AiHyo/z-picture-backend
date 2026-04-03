package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.picture.PictureCategoryQueryRequest;
import com.aih.zpicturebackend.model.entity.PictureCategory;
import com.aih.zpicturebackend.model.vo.PictureCategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PictureCategoryService extends IService<PictureCategory> {

    void validCategoryName(String categoryName);

    QueryWrapper<PictureCategory> getQueryWrapper(PictureCategoryQueryRequest pictureCategoryQueryRequest);

    List<PictureCategoryVO> getPictureCategoryVOList(List<PictureCategory> pictureCategoryList);

    List<String> listCategoryNameList();
}
