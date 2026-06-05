package com.itxc.housekeepbackend.model.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 智能派单算法专用的轻量级候选人对象
 * 剥离了无用信息，大幅降低批量匹配时的内存消耗
 */
@Data
public class CandidateVO {
    
    /**
     * 家政员/员工的主键 ID
     */
    private Long id;
    
    /**
     * 所属企业 ID
     */
    private Long companyId;
    
    /**
     * 该员工针对当前预约服务的能力评分 (用于择优录取)
     */
    private BigDecimal score;
    
}