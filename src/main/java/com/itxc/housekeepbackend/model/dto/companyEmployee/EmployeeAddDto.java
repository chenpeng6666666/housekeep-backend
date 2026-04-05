package com.itxc.housekeepbackend.model.dto.companyEmployee;

import lombok.Data;

/**
 * @author Xy
 * @version 1.0
 * @description: 企业新增员工请求参数
 * @date 2026/4/5 14:23
 */
@Data
public class EmployeeAddDto {

    private String realName;

    private String phone;

    private String password;

    private Integer roleType;

}
