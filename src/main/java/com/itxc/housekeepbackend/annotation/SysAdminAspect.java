package com.itxc.housekeepbackend.annotation;

import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.exception.BusinessException;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.enums.RoleTypeEnum;
import com.itxc.housekeepbackend.service.UserService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Xy
 * @version 1.0
 * @description: 拦截系统管理员权限的接口
 * @date 2026/4/5 17:11
 */
@Aspect
@Component
public class SysAdminAspect {

    @Resource
    private UserService userService;

    @Before("@annotation(com.itxc.housekeepbackend.annotation.CheckSysAdmin)")
    public void doCheck(){
        // 判断当前用户角色 是否为系统管理员
        User loginUser = userService.getLoginUser();
        if (!RoleTypeEnum.ADMIN.getValue().equals(loginUser.getRoleType())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }

    }
}
