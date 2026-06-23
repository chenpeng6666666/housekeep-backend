package com.itxc.housekeepbackend.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AiAssistantResponseVO {

    /**
     * TEXT: 普通答复, SERVICE_INFO: 服务咨询, ORDER_DRAFT: 订单草稿卡片
     */
    private String type;

    /**
     * 展示给用户的自然语言答复
     */
    private String content;

    /**
     * AI 通过工具生成的订单草稿。前端只能展示并引导用户确认，不能直接代表用户下单。
     */
    private OrderDraftCard orderCard;

    /**
     * 服务咨询时可选返回的候选服务卡片
     */
    private List<ServiceCard> services;

    @Data
    public static class OrderDraftCard {
        private Long serviceId;
        private String serviceName;
        private String serviceCoverImg;
        private String serviceDescription;
        private String unit;
        private BigDecimal guidancePrice;
        private Long baseDuration;
        private Integer quantity;
        private BigDecimal estimatedAmount;
        private String remark;
    }

    @Data
    public static class ServiceCard {
        private Long serviceId;
        private String serviceName;
        private String serviceCoverImg;
        private String serviceDescription;
        private String unit;
        private BigDecimal guidancePrice;
        private Long baseDuration;
    }
}
