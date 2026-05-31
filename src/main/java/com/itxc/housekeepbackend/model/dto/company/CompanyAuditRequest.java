package com.itxc.housekeepbackend.model.dto.company;

import lombok.Data;

/**
 * @author Xy
 * @version 1.0
 * @description:
 * @date 2026/5/30 19:40
 */
@Data
public class CompanyAuditRequest {
    // 企业id
    private Long id;
    // 审核状态
    private Integer auditStatus;
    // 拒绝原因
    private String rejectReason;

}
