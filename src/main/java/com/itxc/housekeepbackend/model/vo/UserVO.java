package com.itxc.housekeepbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Xy
 * @version 1.0
 * @description: 脱敏的用户信息
 * @date 2026/4/3 19:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户角色
     */
    private Integer roleType;

    /**
     * 性别 (0: 女, 1: 男, 2: 保密)
     */
    private Integer gender;




}
