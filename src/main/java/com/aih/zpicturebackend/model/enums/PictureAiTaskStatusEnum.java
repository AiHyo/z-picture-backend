package com.aih.zpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PictureAiTaskStatusEnum {

    PENDING("待处理", 0),
    RUNNING("处理中", 1),
    SUCCESS("成功", 2),
    FAILED("失败", 3);

    private final String text;
    private final int value;

    PictureAiTaskStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static PictureAiTaskStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureAiTaskStatusEnum anEnum : PictureAiTaskStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
