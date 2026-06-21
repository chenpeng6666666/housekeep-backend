package com.itxc.housekeepbackend.service;

import com.itxc.housekeepbackend.model.dto.SmartBookingRequestDTO;
import com.itxc.housekeepbackend.model.vo.AiChatMessageVO;
import com.itxc.housekeepbackend.model.vo.AiChatSessionVO;
import com.itxc.housekeepbackend.model.vo.SmartMatchResultVO;
import reactor.core.publisher.Flux;

public interface SmartBookingService {

    /**
     * 多模态智慧服务匹配
     * @param requestDTO 包含文本或图片的请求
     * @return 匹配结果
     */
    SmartMatchResultVO matchService(SmartBookingRequestDTO requestDTO);

    /**
     * 智能家政AI服务助手对话
     * @param message 用户消息
     * @return AI的回复文本
     */
    String chatAssistant(String message);

    /**
     * 智能家政AI服务助手流式对话
     * @param sessionId 会话ID
     * @param message 用户消息
     * @return AI的流式回复文本
     */
    Flux<String> chatAssistantStream(String sessionId, String message);

    /**
     * 获取当前用户的历史会话列表
     */
    java.util.List<AiChatSessionVO> listChatSessions();

    /**
     * 获取指定会话的历史消息记录
     * @param sessionId 会话ID
     */
    java.util.List<AiChatMessageVO> listChatMessages(String sessionId);
}
