package com.aih.zpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class VipExchangeRequest implements Serializable {

    private static final long serialVersionUID = -4172144122762683341L;

    /**
     * 会员兑换码
     */
    private String vipCode;
}
