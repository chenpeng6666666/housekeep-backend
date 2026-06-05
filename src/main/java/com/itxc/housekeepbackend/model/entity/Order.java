package com.itxc.housekeepbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 服务预约订单表
 * @TableName order
 */
@TableName("`order`")
@Data
public class Order implements Serializable {
    /**
     * 主键ID (雪花算法)
     */
    @TableId
    private Long id;

    /**
     * 订单编号 (如: ORD20260601xxxx)
     */
    private String orderNo;

    /**
     * 下单C端用户ID
     */
    private Long userId;

    /**
     * 服务项目ID
     */
    private Long serviceId;

    /**
     * 分配企业ID
     */
    private Long companyId;

    /**
     * 分配员工ID
     */
    private Long employeeId;

    /**
     * 服务地址ID
     */
    private Long addressId;

    /**
     * 期望上门服务时间
     */
    private Date serviceTime;

    /**
     * 预计服务结束时间
     */
    private Date estimatedEndTime;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 订单总金额 (后端计算)
     */
    private BigDecimal totalAmount;

    /**
     * 阿姨星级要求(0:不限, 4.0:优质, 4.8:金牌)
     */
    private BigDecimal requireScore;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 用户需求图片
     */
    private String requirementImg;

    /**
     * 订单状态: 0-待派单, 1-已接单待上门, 2-服务中, 3-已完成, 4-已取消
     */
    private Integer status;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}