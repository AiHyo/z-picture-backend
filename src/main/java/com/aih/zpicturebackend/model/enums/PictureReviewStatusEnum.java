package com.aih.zpicturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审核", 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }


    /**
     * 根据value获取枚举
     */
    public static PictureReviewStatusEnum getEnumByValue(int value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        // 如果枚举类多可以使用map缓存
        for (PictureReviewStatusEnum e : PictureReviewStatusEnum.values()) {
            if (e.value == value) {
                return e;
            }
        }
        return null;
    }
}
