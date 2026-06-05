package com.itxc.housekeepbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 员工工单排班占用表
 * @TableName employee_schedule
 */
@TableName(value ="employee_schedule")
@Data
public class EmployeeSchedule implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 关联的订单编号
     */
    private String orderNo;

    /**
     * 服务开始占用时间
     */
    private Date startTime;

    /**
     * 服务结束释放时间 (通常为开始时间 + 服务预估时长)
     */
    private Date endTime;

    /**
     * 
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}