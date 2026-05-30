package com.itxc.housekeepbackend.model.vo;

import lombok.Data;


@Data
public class EmployeeVO {

    /**
     * 员工主键ID
     */
    private Long id;

    /**
     * 员工姓名
     */
    private String realName;

    /**
     * 角色: ADMIN-超级管理员, STAFF-普通员工
     */
    private String roleType;

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


    /**
     * 企业审核状态：0-待完善信息(草稿), 1-待平台审核, 2-审核通过, 3-审核驳回
     */
    private Integer auditStatus;


}
