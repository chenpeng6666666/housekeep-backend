package com.itxc.housekeepbackend.model.dto.admin;

import lombok.Data;

@Data
public class AdminLoginDto {

    private String username; // 注意这里是 username，不是 phone

    private String password;
}