package com.aih.zpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PictureReportStatusEnum {

    PENDING("待处理", 0),
    APPROVED("举报成立", 1),
    REJECTED("举报驳回", 2);

    private final String text;
    private final int value;

    PictureReportStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static PictureReportStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReportStatusEnum anEnum : PictureReportStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
