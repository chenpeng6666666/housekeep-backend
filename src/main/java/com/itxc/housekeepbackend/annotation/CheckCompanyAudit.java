package com.itxc.housekeepbackend.annotation;

import java.lang.annotation.*;

/**
 * 校验企业是否已通过审核
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckCompanyAudit {
}