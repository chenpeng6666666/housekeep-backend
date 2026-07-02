package com.itxc.housekeepbackend.enums;

import lombok.Getter;

/**
 * AI 流式对话事件类型
 */
@Getter
public enum ChatEventTypeEnum {
    /**
     * 数据事件（文本增量）
     */
    DATA(1001),
    /**
     * 停止事件
     */
    STOP(1002),
    /**
     * 参数/工具事件（卡片、服务列表等）
     */
    PARAM(1003);

    private final int value;

    ChatEventTypeEnum(int value) {
        this.value = value;
    }
}
