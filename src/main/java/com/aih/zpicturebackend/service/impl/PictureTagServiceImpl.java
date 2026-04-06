package com.aih.zpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aih.zpicturebackend.mapper.PictureTagMapper;
import com.aih.zpicturebackend.model.dto.picture.PictureTagQueryRequest;
import com.aih.zpicturebackend.model.entity.PictureTag;
import com.aih.zpicturebackend.model.vo.PictureTagVO;
import com.aih.zpicturebackend.service.PictureTagService;
import com.aih.zpicturebackend.utils.PictureMetaUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PictureTagServiceImpl extends ServiceImpl<PictureTagMapper, PictureTag>
        implements PictureTagService {

    @Override
    public void validTagName(String tagName) {
        PictureMetaUtils.validateTag(tagName);
    }

    @Override
    public QueryWrapper<PictureTag> getQueryWrapper(PictureTagQueryRequest pictureTagQueryRequest) {
        QueryWrapper<PictureTag> queryWrapper = new QueryWrapper<>();
        if (pictureTagQueryRequest == null) {
            return queryWrapper;
        }
        queryWrapper.like(StrUtil.isNotBlank(pictureTagQueryRequest.getTagName()), "tagName", pictureTagQueryRequest.getTagName());
        queryWrapper.orderByAsc("id");
        return queryWrapper;
    }

    @Override
    public List<PictureTagVO> getPictureTagVOList(List<PictureTag> pictureTagList) {
        if (CollUtil.isEmpty(pictureTagList)) {
            return Collections.emptyList();
        }
        return pictureTagList.stream().map(PictureTagVO::objToVo).collect(Collectors.toList());
    }

    @Override
    public List<String> listTagNameList() {
        return this.lambdaQuery()
                .orderByAsc(PictureTag::getId)
                .list()
                .stream()
                .map(PictureTag::getTagName)
                .collect(Collectors.toList());
    }
}
