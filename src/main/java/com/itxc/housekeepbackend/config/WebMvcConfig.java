package com.itxc.housekeepbackend.config;

import com.itxc.housekeepbackend.interceptor.AuthInterceptor;
import com.itxc.housekeepbackend.utils.StorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Resource
    private StorageProperties storageProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                // 👇 核心修复：放行本地图片的访问前缀，使其不经过登录校验
                .excludePathPatterns("/uploads/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 获取 YAML 中的路径并统一替换斜杠
        String basePath = storageProperties.getLocalBasePath().replace("\\", "/");

        // 2. 确保以斜杠结尾
        if (!basePath.endsWith("/")) {
            basePath = basePath + "/";
        }

        // 3. 🚨 终极安全映射写法：直接用 file: 加上绝对路径 (例如 file:D:/img/uploads/)
        String resourceLocation = "file:" + basePath;

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        // 4. 打印出来确认一下，启动时留意控制台！
        System.out.println("==================================================");
        System.out.println("🚀 静态资源映射已启动: /uploads/** -> " + resourceLocation);
        System.out.println("==================================================");
    }
}