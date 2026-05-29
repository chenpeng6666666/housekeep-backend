package com.itxc.housekeepbackend.config;

import com.itxc.housekeepbackend.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有请求，具体的放行逻辑交由 AuthInterceptor 内部的注解判断来决定
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
    }
}