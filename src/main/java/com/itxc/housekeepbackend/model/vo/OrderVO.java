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
    private Long companyId;
    private Long employeeId;
    private Long addressId;
    private Date serviceTime;
    private Date estimatedEndTime;
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

    // === 扩展字段：服务地址信息 ===
    private String contactName;
    private String contactPhone;
    private String detailAddress;

    // === 扩展字段：下单用户信息 ===
    private String userNickname;
    private String userPhone;
}