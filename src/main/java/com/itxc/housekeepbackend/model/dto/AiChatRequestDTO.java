package com.itxc.housekeepbackend.model.dto;

import lombok.Data;

/**
 * 智能助手对话请求 DTO
 */
@Data
public class AiChatRequestDTO {
    /**
     * 用户输入的对话文本
     */
    private String message;

    /**
     * 会话ID，用于上下文记忆
     */
    private String sessionId;
}
