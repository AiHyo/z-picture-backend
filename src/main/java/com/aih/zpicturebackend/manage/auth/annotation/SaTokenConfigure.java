package com.aih.zpicturebackend.manage.auth.annotation;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    // 注册 Sa-Token 拦截器，打开注解式鉴权功能
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功能
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }
    // @SaCheckLogin(type = StpUserUtil.TYPE)。
    // 在多账号体系下，每次都要指定账号体系的 loginType, 不够优雅
    // https://sa-token.cc/doc.html#/up/many-account?id=_7%e3%80%81%e4%bd%bf%e7%94%a8%e6%b3%a8%e8%a7%a3%e5%90%88%e5%b9%b6%e7%ae%80%e5%8c%96%e4%bb%a3%e7%a0%81

    // 使用注解合并, 简化使用：可以自定义注解(在注解中指定type), 直接使用自定义注解即可 [更优雅]
    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写Sa-Token的注解处理器，增加注解合并功能
        SaAnnotationStrategy.instance.getAnnotation = (element, annotationClass) -> {
            return AnnotatedElementUtils.getMergedAnnotation(element, annotationClass);
        };
    }
}
