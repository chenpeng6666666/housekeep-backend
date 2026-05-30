package com.itxc.housekeepbackend.constant;

/**
 * 状态常量
 */
public interface StatusConstant {
    /**
     * 状态 0-正常 1-禁用
     */
    Integer STATUS_NORMAL = 1;
    Integer STATUS_DISABLE = 0;

    //审核状态：0-待完善信息(草稿), 1-待平台审核, 2-审核通过, 3-审核驳回
    Integer AUDIT_STATUS_DRAFT = 0;
    Integer AUDIT_STATUS_PENDING = 1;
    Integer AUDIT_STATUS_SUCCESS = 2;
    Integer AUDIT_STATUS_REJECT = 3;
}
