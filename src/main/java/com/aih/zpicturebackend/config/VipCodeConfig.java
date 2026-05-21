package com.aih.zpicturebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vip-code")
@Data
public class VipCodeConfig {

    /**
     * 运行时兑换码文件路径
     */
    private String filePath = "data/vip-codes.json";
}
