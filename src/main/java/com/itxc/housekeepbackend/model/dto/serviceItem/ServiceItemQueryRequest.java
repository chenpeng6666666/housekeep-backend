package com.itxc.housekeepbackend.model.dto.serviceItem;

import lombok.Data;

@Data
public class ServiceItemQueryRequest {
    private Integer current = 1;
    private Integer pageSize = 10;
    
    /** 按分类筛选 */
    private Long categoryId;
    
    /** 模糊搜索服务名称 */
    private String name;
}