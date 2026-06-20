package com.itxc.housekeepbackend.model.vo;

import lombok.Data;

@Data
public class SmartMatchResultVO {
    /**
     * 匹配到的服务项ID
     */
    private Long serviceId;
    
    /**
     * 匹配到的服务项名称
     */
    private String serviceName;

    /**
     * 大模型提取的关键意图词（如：开荒保洁）
     */
    private String keyword;

    /**
     * 提取出的特殊要求/备注信息（自动填入订单备注）
     */
    private String remark;

    /**
     * 建议的服务数量（提取出的面积/时长转换，若无法提取默认 1）
     */
    private Integer quantity;
}
