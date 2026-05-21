package com.aih.zpicturebackend.manager.vip;

import com.aih.zpicturebackend.config.VipCodeConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.nio.file.Paths;

@Configuration
public class VipCodeStoreFactory {

    @Resource
    private VipCodeConfig vipCodeConfig;

    @Resource
    private ObjectMapper objectMapper;

    @Bean
    public VipCodeStore vipCodeStore() {
        return new VipCodeStore(Paths.get(vipCodeConfig.getFilePath()), objectMapper);
    }
}
