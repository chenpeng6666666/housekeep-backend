package com.itxc.housekeepbackend.annotation;

import java.lang.annotation.*;

/**
 * 统一鉴权注解
 * 可以作用在 Controller 类上（该类所有方法都需要鉴权），也可以作用在单个方法上
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAuth {
    
    /**
     * 需要的权限角色：默认是普通用户
     * 可选值： "admin" (平台管理员), "company" (企业), "user" (普通用户)
     */
    String value() default "user";
}