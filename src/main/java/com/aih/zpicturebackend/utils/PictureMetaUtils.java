package com.aih.zpicturebackend.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 图片分类和标签的统一规范化工具。
 */
public final class PictureMetaUtils {

    public static final int META_MAX_LENGTH = 64;
    public static final int CATEGORY_SUGGESTION_LIMIT = 12;
    public static final int TAG_SUGGESTION_LIMIT = 20;

    public static final List<String> DEFAULT_TAG_LIST = Collections.unmodifiableList(Arrays.asList(
            "风光", "人文", "城市", "艺术", "游戏", "动物", "植物", "抽象", "明星", "动漫感"
    ));

    public static final List<String> DEFAULT_CATEGORY_LIST = Collections.unmodifiableList(Arrays.asList(
            "静物", "动态", "特别", "极简", "复古", "特写", "航拍", "天气", "光影", "夜色", "色彩"
    ));

    private PictureMetaUtils() {
    }

    public static String normalizeCategory(String category) {
        return normalizeMetaValue(category);
    }

    public static String normalizeTag(String tag) {
        return normalizeMetaValue(tag);
    }

    public static String normalizeSearchText(String searchText) {
        return normalizeMetaValue(searchText);
    }

    public static List<String> normalizeTags(List<String> tags) {
        if (CollUtil.isEmpty(tags)) {
            return new ArrayList<>();
        }
        LinkedHashSet<String> tagSet = new LinkedHashSet<>();
        for (String tag : tags) {
            String normalizedTag = normalizeTag(tag);
            if (normalizedTag == null) {
                continue;
            }
            validateMetaValueLength(normalizedTag, "标签");
            tagSet.add(normalizedTag);
        }
        return new ArrayList<>(tagSet);
    }

    public static String toTagsJson(List<String> tags) {
        List<String> normalizedTags = normalizeTags(tags);
        if (CollUtil.isEmpty(normalizedTags)) {
            return null;
        }
        return JSONUtil.toJsonStr(normalizedTags);
    }

    public static List<String> parseTags(String tagsJson) {
        if (StrUtil.isBlank(tagsJson) || "null".equalsIgnoreCase(tagsJson)) {
            return new ArrayList<>();
        }
        try {
            return normalizeTags(JSONUtil.toList(tagsJson, String.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static List<String> mergeSuggestions(int limit, List<String>... suggestionLists) {
        LinkedHashSet<String> mergedSet = new LinkedHashSet<>();
        if (suggestionLists != null) {
            for (List<String> suggestionList : suggestionLists) {
                if (CollUtil.isEmpty(suggestionList)) {
                    continue;
                }
                for (String value : suggestionList) {
                    String normalizedValue = normalizeMetaValue(value);
                    if (normalizedValue == null) {
                        continue;
                    }
                    mergedSet.add(normalizedValue);
                    if (mergedSet.size() >= limit) {
                        return new ArrayList<>(mergedSet);
                    }
                }
            }
        }
        return new ArrayList<>(mergedSet);
    }

    public static void validateCategory(String category) {
        String normalizedCategory = normalizeCategory(category);
        if (normalizedCategory == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类名称不能为空");
        }
        validateMetaValueLength(normalizedCategory, "分类");
    }

    public static void validateTag(String tag) {
        String normalizedTag = normalizeTag(tag);
        if (normalizedTag == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签名称不能为空");
        }
        validateMetaValueLength(normalizedTag, "标签");
    }

    public static void validateMetaValueLength(String value, String fieldName) {
        if (StrUtil.isNotBlank(value) && value.length() > META_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, fieldName + "名称过长");
        }
    }

    private static String normalizeMetaValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        String normalizedValue = StrUtil.trim(value);
        return StrUtil.isBlank(normalizedValue) ? null : normalizedValue;
    }
}
