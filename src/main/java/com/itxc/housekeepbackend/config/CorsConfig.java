package com.itxc.housekeepbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域请求配置
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // 覆盖所有请求
//        registry.addMapping("/**")
//                // 允许发送 Cookie
//                .allowCredentials(true)
//                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
//                .allowedOriginPatterns("*")
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .exposedHeaders("*");
//    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 🚨 核心修复：允许带 Cookie，此时 Origins 不能写 *，必须明确前端地址
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5174"); // 你的前端地址
        config.addAllowedOrigin("http://127.0.0.1:5174");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法 (GET, POST, OPTIONS 等)
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}