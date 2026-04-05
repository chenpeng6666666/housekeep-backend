package com.itxc.housekeepbackend.constant;

/**
 * 审核状态: 0-待审核, 1-审核通过, 2-审核驳回
 */
public interface CompanyConstant {

    Integer AUDIT_STATUS_WAIT = 0;
    Integer AUDIT_STATUS_PASS = 1;
    Integer AUDIT_STATUS_REJECT = 2;

}
