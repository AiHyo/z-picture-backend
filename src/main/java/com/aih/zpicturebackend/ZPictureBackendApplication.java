package com.aih.zpicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class}) // 暂时不启用分表功能
@EnableAsync // 开启方法的@Async注解, 使其异步调用
@MapperScan("com.aih.zpicturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 开启后在代码中可以使用AopContext.currentProxy() 获取代理对象
public class ZPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZPictureBackendApplication.class, args);
    }

}
