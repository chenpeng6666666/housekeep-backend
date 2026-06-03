package com.itxc.housekeepbackend.model.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderSubmitDTO {
    private Long serviceId;

    private Long addressId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date serviceTime;

    private Integer quantity;

    private BigDecimal requireScore; // 0.0, 4.0, 4.8

    private String remark;

    private String requirementImg;
    
    // 前端传过来的总价仅供参考或对账，实际金额由后端计算
    private BigDecimal totalAmount; 
}