package com.itxc.housekeepbackend.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmployeeSkillVO {
    private Long serviceId;   // 服务项目ID
    private BigDecimal score; // 能力评分
}