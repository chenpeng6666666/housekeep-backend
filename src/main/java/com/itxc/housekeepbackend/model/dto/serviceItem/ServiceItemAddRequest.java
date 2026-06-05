package com.itxc.housekeepbackend.model.dto.serviceItem;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceItemAddRequest {
    private Long id; // 如果传了id就是更新，没传就是新增
    private Long categoryId;
    private String name;
    private String coverImg;
    private String unit;
    private BigDecimal guidancePrice;
    private Long baseDuration;
    private Integer sort;
    private String description;
    // 上下架状态更新专用
    private Integer status; 
}