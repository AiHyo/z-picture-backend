package com.aih.zpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.aih.zpicturebackend.mapper.PictureCategoryMapper;
import com.aih.zpicturebackend.model.dto.picture.PictureCategoryQueryRequest;
import com.aih.zpicturebackend.model.entity.PictureCategory;
import com.aih.zpicturebackend.model.vo.PictureCategoryVO;
import com.aih.zpicturebackend.service.PictureCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PictureCategoryServiceImpl extends ServiceImpl<PictureCategoryMapper, PictureCategory>
        implements PictureCategoryService {

    @Override
    public void validCategoryName(String categoryName) {
        if (StrUtil.isBlank(categoryName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        }
        if (categoryName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称过长");
        }
    }

    @Override
    public QueryWrapper<PictureCategory> getQueryWrapper(PictureCategoryQueryRequest pictureCategoryQueryRequest) {
        QueryWrapper<PictureCategory> queryWrapper = new QueryWrapper<>();
        if (pictureCategoryQueryRequest == null) {
            return queryWrapper;
        }
        queryWrapper.like(StrUtil.isNotBlank(pictureCategoryQueryRequest.getCategoryName()), "categoryName", pictureCategoryQueryRequest.getCategoryName());
        queryWrapper.orderByAsc("id");
        return queryWrapper;
    }

    @Override
    public List<PictureCategoryVO> getPictureCategoryVOList(List<PictureCategory> pictureCategoryList) {
        if (CollUtil.isEmpty(pictureCategoryList)) {
            return Collections.emptyList();
        }
        return pictureCategoryList.stream().map(PictureCategoryVO::objToVo).collect(Collectors.toList());
    }

    @Override
    public List<String> listCategoryNameList() {
        return this.lambdaQuery()
                .orderByAsc(PictureCategory::getId)
                .list()
                .stream()
                .map(PictureCategory::getCategoryName)
                .collect(Collectors.toList());
    }
}
