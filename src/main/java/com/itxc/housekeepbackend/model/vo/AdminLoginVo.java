package com.itxc.housekeepbackend.model.vo;

import lombok.Data;

@Data
public class AdminLoginVo {

    private Long id;

    private String username;

    private String realName;

    private String avatar;

    private String token;
}