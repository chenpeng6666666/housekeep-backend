package com.itxc.housekeepbackend.controller;

import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.dto.AiChatRequestDTO;
import com.itxc.housekeepbackend.model.dto.SmartBookingRequestDTO;
import com.itxc.housekeepbackend.model.vo.AiAssistantResponseVO;
import com.itxc.housekeepbackend.model.vo.AiChatMessageVO;
import com.itxc.housekeepbackend.model.vo.AiChatSessionVO;
import com.itxc.housekeepbackend.model.vo.SmartMatchResultVO;
import com.itxc.housekeepbackend.service.SmartBookingService;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Flux;

/**
 * 智慧服务预约控制器
 */
@RestController
@RequestMapping("/smart")
public class SmartBookingController {

    @Resource
    private SmartBookingService smartBookingService;

    /**
     * 多模态智能预约服务匹配
     *
     * @param requestDTO 智慧预约请求参数
     * @return 智能匹配出的标准服务和建议内容
     */
    @PostMapping("/match")
    public BaseResponse<SmartMatchResultVO> matchService(@RequestBody SmartBookingRequestDTO requestDTO) {
        SmartMatchResultVO result = smartBookingService.matchService(requestDTO);
        return ResultUtils.success(result);
    }

    /**
     * AI智能服务助手对话
     *
     * @param requestDTO 对话请求参数
     * @return AI的回复文本
     */
    @PostMapping("/chat")
    public BaseResponse<String> chatAssistant(@RequestBody AiChatRequestDTO requestDTO) {
        String message = requestDTO == null ? "" : requestDTO.getMessage();
        String result = smartBookingService.chatAssistant(message);
        return ResultUtils.success(result);
    }

    /**
     * AI 智能体对话：通过后端工具查询服务、生成订单草稿卡片。
     */
    @PostMapping("/agent/chat")
    public BaseResponse<AiAssistantResponseVO> chatAgent(@RequestBody AiChatRequestDTO requestDTO) {
        String message = requestDTO == null ? "" : requestDTO.getMessage();
        String sessionId = requestDTO == null ? "" : requestDTO.getSessionId();
        return ResultUtils.success(smartBookingService.chatAgent(sessionId, message));
    }

    /**
     * AI智能服务助手对话（流式输出 SSE）
     *
     * @param requestDTO 对话请求参数
     * @return 文本事件流 Flux<String>
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatAssistantStream(@RequestBody AiChatRequestDTO requestDTO,
                                                             HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");
        String message = requestDTO == null ? "" : requestDTO.getMessage();
        String sessionId = requestDTO == null ? "" : requestDTO.getSessionId();
        return smartBookingService.chatAssistantStream(sessionId, message)
                .map(chunk -> ServerSentEvent.builder(chunk).build());
    }

    /**
     * 获取历史会话列表
     */
    @GetMapping("/chat/sessions")
    public BaseResponse<java.util.List<AiChatSessionVO>> listChatSessions() {
        return ResultUtils.success(smartBookingService.listChatSessions());
    }

    /**
     * 获取指定会话的历史消息
     */
    @GetMapping("/chat/messages")
    public BaseResponse<java.util.List<AiChatMessageVO>> listChatMessages(@RequestParam("sessionId") String sessionId) {
        return ResultUtils.success(smartBookingService.listChatMessages(sessionId));
    }
}
