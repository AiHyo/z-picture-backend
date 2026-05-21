package com.aih.zpicturebackend.manager.vip;

import lombok.Data;

@Data
public class VipCodeRecord {

    /**
     * 兑换码
     */
    private String code;

    /**
     * 是否已使用
     */
    private boolean used;

    /**
     * 使用用户 id
     */
    private Long usedUserId;

    /**
     * 使用时间
     */
    private String usedTime;

    /**
     * 兑换码过期时间
     */
    private String expireTime;

    /**
     * 兑换后增加的会员天数
     */
    private Integer durationDays;

    /**
     * 会员等级：1-专业会员，2-旗舰会员
     */
    private Integer vipLevel;
}
