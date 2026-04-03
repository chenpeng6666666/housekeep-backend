package com.itxc.housekeepbackend.model.dto.user;

import lombok.Data;

/**
 * @author Xy
 * @version 1.0
 * @description: 用户登录参数
 * @date 2026/4/3 18:16
 */
@Data
public class UserLoginDto {

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;



}
