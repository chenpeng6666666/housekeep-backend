package com.itxc.housekeepbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 流式对话的响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEventVO {

    /**
     * 文本或卡片对象内容
     */
    private Object eventData;

    /**
     * 事件类型，1001-数据事件，1002-停止事件，1003-参数事件
     */
    private int eventType;

}
