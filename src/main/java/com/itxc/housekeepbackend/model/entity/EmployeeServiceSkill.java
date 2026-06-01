package com.itxc.housekeepbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 员工服务技能与能力评分表
 * @TableName employee_service_skill
 */
@TableName(value ="employee_service_skill")
@Data
public class EmployeeServiceSkill implements Serializable {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 员工ID (关联 company_employee)
     */
    private Long employeeId;

    /**
     * 服务项目ID (关联 service_item)
     */
    private Long serviceId;

    /**
     * 能力评分 (例如: 1.0~5.0 分)
     */
    private BigDecimal score;

    /**
     * 
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}