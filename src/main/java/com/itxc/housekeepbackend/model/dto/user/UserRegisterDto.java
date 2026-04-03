package com.itxc.housekeepbackend.model.dto.user;

import lombok.Data;

/**
 * @author Xy
 * @version 1.0
 * @description: 用户注册请求参数
 * @date 2026/4/3 20:12
 */
@Data
public class UserRegisterDto {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 手机验证码
     */
    private String code;
}
