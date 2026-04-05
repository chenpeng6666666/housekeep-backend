package com.itxc.housekeepbackend.model.vo;

import lombok.Data;

/**
 * 企业用户脱敏信息
 */
@Data
public class CompanyEmployeeLoginVO {

    private Long id;

    private String realName;

    private String phone;

    private Integer roleType;

    private Long companyId;

    private String companyName;

    private Integer auditStatus; // 传回企业审核状态，方便前端做路由拦截

    private String token;
}