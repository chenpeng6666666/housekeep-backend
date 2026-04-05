package com.itxc.housekeepbackend.model.vo;

import lombok.Data;

/**
 * @author Xy
 * @version 1.0
 * @description:  企业用户登录后的信息
 * @date 2026/4/5 12:45
 */
@Data
public class EmployeeVO {

    /**
     * 员工姓名
     */
    private String realName;

    /**
     * 联系电话/登录账号
     */
    private String phone;

    /**
     * 所属企业ID
     */
    private Long companyId;

    /**
     * 企业名称
     */
    private String companyName;
}
