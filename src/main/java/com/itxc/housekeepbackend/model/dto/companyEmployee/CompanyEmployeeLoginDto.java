package com.itxc.housekeepbackend.model.dto.companyEmployee;

import lombok.Data;

/**
 * 企业用户登录参数
 */
@Data
public class CompanyEmployeeLoginDto {

    private String phone;

    private String password;
}