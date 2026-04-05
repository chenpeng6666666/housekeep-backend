package com.itxc.housekeepbackend.model.dto.company;

import lombok.Data;

/**
 * 企业入驻请求参数
 */
@Data
public class CompanyRegisterDto {
    private String companyName;
    private String licenseNo;
//    private String contactName;
    
    // 以下两个字段用于自动生成企业的默认管理员账号
    private String adminPhone; 
    private String password;
}