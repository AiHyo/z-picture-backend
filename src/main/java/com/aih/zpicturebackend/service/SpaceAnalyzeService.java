package com.aih.zpicturebackend.service;

import com.aih.zpicturebackend.model.dto.space.SpaceAddRequest;
import com.aih.zpicturebackend.model.dto.space.SpaceQueryRequest;
import com.aih.zpicturebackend.model.dto.space.analyze.*;
import com.aih.zpicturebackend.model.entity.Space;
import com.aih.zpicturebackend.model.entity.User;
import com.aih.zpicturebackend.model.vo.SpaceVO;
import com.aih.zpicturebackend.model.vo.space.analyze.*;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author AiH-Madam
 * @description 针对表【spaceAnalyze(空间分析)】的数据库操作Service
 */
public interface SpaceAnalyzeService extends IService<Space> {


    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
