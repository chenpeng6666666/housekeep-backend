package com.itxc.housekeepbackend.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderVO {
    // === 订单原有字段 ===
    private Long id;
    private String orderNo;
    private Long userId;
    private Long serviceId;
    private Long addressId;
    private Date serviceTime;
    private Integer quantity;
    private BigDecimal totalAmount;
    private BigDecimal requireScore;
    private String requirementImg; // 多模态需求图
    private String remark;
    private Integer status;
    private Date createTime;

    // ===  扩展字段：关联的服务项目信息 ===
    private String serviceName;
    private String serviceCoverImg;
    private String serviceDescription; // 描述信息
    private String companyName;
    private String employeeName;
}