package com.aih.zpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum SpaceInviteStatusEnum {

    PENDING("待处理", 0),
    ACCEPTED("已接受", 1),
    REJECTED("已拒绝", 2),
    CANCELED("已取消", 3);

    private final String text;
    private final int value;

    SpaceInviteStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static SpaceInviteStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceInviteStatusEnum anEnum : SpaceInviteStatusEnum.values()) {
            if (anEnum.value == value) {
                return anEnum;
            }
        }
        return null;
    }
}
