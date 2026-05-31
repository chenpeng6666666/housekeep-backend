package com.itxc.housekeepbackend.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {

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
     * 性别 (0: 女, 1: 男, 2: 保密)
     */
    private Integer gender;


}