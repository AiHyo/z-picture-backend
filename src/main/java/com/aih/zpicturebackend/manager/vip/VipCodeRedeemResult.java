package com.aih.zpicturebackend.manager.vip;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VipCodeRedeemResult {

    /**
     * 兑换码
     */
    private String code;

    /**
     * 兑换后增加的会员天数
     */
    private int durationDays;

    /**
     * 会员等级：1-专业会员，2-旗舰会员
     */
    private int vipLevel;
}
