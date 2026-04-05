package com.itxc.housekeepbackend.annotation;

import java.lang.annotation.*;

/**
 * 检验系统管理员
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckSysAdmin {
}
