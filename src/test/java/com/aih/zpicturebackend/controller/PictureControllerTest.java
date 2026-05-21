package com.aih.zpicturebackend.controller;

import com.aih.zpicturebackend.common.DeleteRequest;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.model.entity.Picture;
import com.aih.zpicturebackend.model.entity.PictureCategory;
import com.aih.zpicturebackend.model.entity.PictureTag;
import com.aih.zpicturebackend.service.PictureCategoryService;
import com.aih.zpicturebackend.service.PictureService;
import com.aih.zpicturebackend.service.PictureTagService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PictureControllerTest {

    private PictureController pictureController;
    private PictureService pictureService;
    private PictureTagService pictureTagService;
    private PictureCategoryService pictureCategoryService;

    @BeforeEach
    void setUp() {
        pictureController = new PictureController();
        pictureService = mock(PictureService.class);
        pictureTagService = mock(PictureTagService.class);
        pictureCategoryService = mock(PictureCategoryService.class);
        ReflectionTestUtils.setField(pictureController, "pictureService", pictureService);
        ReflectionTestUtils.setField(pictureController, "pictureTagService", pictureTagService);
        ReflectionTestUtils.setField(pictureController, "pictureCategoryService", pictureCategoryService);
    }

    @Test
    void deletePictureTagShouldRejectWhenPictureUsesTag() {
        PictureTag tag = new PictureTag();
        tag.setId(1L);
        tag.setTagName("艺术");
        when(pictureTagService.getById(1L)).thenReturn(tag);
        LambdaQueryChainWrapper<Picture> query = mockPictureQuery();
        when(pictureService.lambdaQuery()).thenReturn(query);
        when(query.like(any(), eq("\"艺术\""))).thenReturn(query);
        when(query.exists()).thenReturn(true);

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setId(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pictureController.deletePictureTag(deleteRequest)
        );
        assertEquals("该标签已被图片使用，无法删除", exception.getMessage());
        verify(pictureTagService, never()).removeById(any());
    }

    @Test
    void deletePictureCategoryShouldRejectWhenPictureUsesCategory() {
        PictureCategory category = new PictureCategory();
        category.setId(2L);
        category.setCategoryName("风景");
        when(pictureCategoryService.getById(2L)).thenReturn(category);
        LambdaQueryChainWrapper<Picture> query = mockPictureQuery();
        when(pictureService.lambdaQuery()).thenReturn(query);
        when(query.eq(any(), eq("风景"))).thenReturn(query);
        when(query.exists()).thenReturn(true);

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setId(2L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> pictureController.deletePictureCategory(deleteRequest)
        );
        assertEquals("该分类已被图片使用，无法删除", exception.getMessage());
        verify(pictureCategoryService, never()).removeById(any());
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryChainWrapper<Picture> mockPictureQuery() {
        return mock(LambdaQueryChainWrapper.class);
    }
}
