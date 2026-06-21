package com.itxc.housekeepbackend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.itxc.housekeepbackend.mapper.ServiceItemMapper;
import com.itxc.housekeepbackend.model.dto.SmartBookingRequestDTO;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.SmartMatchResultVO;
import com.itxc.housekeepbackend.service.SmartBookingService;
import com.itxc.housekeepbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import jakarta.annotation.Resource;
import reactor.core.publisher.Flux;

import java.net.URL;
import java.util.List;

@Service
@Slf4j
public class SmartBookingServiceImpl implements SmartBookingService {

    @Resource
    private ServiceItemMapper serviceItemMapper;

    private final ChatClient chatClient;

    @Resource
    private UserService userService;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    public SmartBookingServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public SmartMatchResultVO matchService(SmartBookingRequestDTO requestDTO) {
        SmartMatchResultVO result = new SmartMatchResultVO();
        String text = requestDTO.getText();
        String imageUrl = requestDTO.getImageUrl();

        result.setQuantity(1);
        result.setKeyword("日常保洁");
        result.setRemark(text);

        if (StringUtils.isBlank(text) && StringUtils.isBlank(imageUrl)) {
            setDefaultService(result);
            return result;
        }

        try {
            if (!"YOUR_API_KEY".equals(apiKey) && StringUtils.isNotBlank(apiKey)) {
                // 构建系统指令
                String systemText = "你是一个专业的家政智能调度专家。请分析用户的输入（文本和图片），提取核心家政服务关键字、数量和润色后的备注。" +
                        "严格以JSON格式输出，不要任何多余字符。格式：{\"keyword\":\"保洁/擦玻璃/家电清洗等\",\"quantity\":1,\"remark\":\"重写的话术\"}";

                // 构建用户输入
                String userText = StringUtils.isNotBlank(text) ? "用户的需求是：" + text : "请根据图片分析需求。";
                
                org.springframework.ai.chat.messages.Message systemMessage = new org.springframework.ai.chat.messages.SystemMessage(systemText);
                
                String aiResponse;
                if (StringUtils.isNotBlank(imageUrl)) {
                    // 多模态调用
                    UserMessage userMessage = new UserMessage(userText, 
                            List.of(new org.springframework.ai.model.Media(MimeTypeUtils.IMAGE_JPEG, new URL(imageUrl))));
                    org.springframework.ai.chat.prompt.Prompt prompt = new org.springframework.ai.chat.prompt.Prompt(List.of(systemMessage, userMessage));
                    
                    aiResponse = chatClient.prompt(prompt)
                            .call()
                            .content();
                } else {
                    // 纯文本调用
                    UserMessage userMessage = new UserMessage(userText);
                    org.springframework.ai.chat.prompt.Prompt prompt = new org.springframework.ai.chat.prompt.Prompt(List.of(systemMessage, userMessage));
                    aiResponse = chatClient.prompt(prompt)
                            .call()
                            .content();
                }

                log.info("Spring AI 返回结果: {}", aiResponse);
                
                // 提取 JSON
                String jsonStr = aiResponse;
                if (jsonStr.startsWith("```json")) jsonStr = jsonStr.substring(7);
                if (jsonStr.startsWith("```")) jsonStr = jsonStr.substring(3);
                if (jsonStr.endsWith("```")) jsonStr = jsonStr.substring(0, jsonStr.length() - 3);

                JSONObject jsonObj = JSONUtil.parseObj(jsonStr.trim());
                String aiKeyword = jsonObj.getStr("keyword");
                Integer aiQuantity = jsonObj.getInt("quantity");
                String aiRemark = jsonObj.getStr("remark");

                if (StringUtils.isNotBlank(aiKeyword)) result.setKeyword(aiKeyword);
                if (aiQuantity != null && aiQuantity > 0) result.setQuantity(aiQuantity);
                if (StringUtils.isNotBlank(aiRemark)) result.setRemark(aiRemark);
            }
        } catch (Exception e) {
            log.error("Spring AI 调用异常，降级到默认规则", e);
        }

        // 数据库模糊匹配
        LambdaQueryWrapper<ServiceItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceItem::getStatus, 1);
        queryWrapper.like(ServiceItem::getName, result.getKeyword());
        List<ServiceItem> serviceItems = serviceItemMapper.selectList(queryWrapper);

        ServiceItem matchedItem = null;
        if (!serviceItems.isEmpty()) {
            matchedItem = serviceItems.get(0);
        } else {
            LambdaQueryWrapper<ServiceItem> backupWrapper = new LambdaQueryWrapper<>();
            backupWrapper.eq(ServiceItem::getStatus, 1);
            backupWrapper.like(ServiceItem::getName, "保洁");
            List<ServiceItem> backupItems = serviceItemMapper.selectList(backupWrapper);
            if (!backupItems.isEmpty()) {
                matchedItem = backupItems.get(0);
            }
        }

        if (matchedItem != null) {
            result.setServiceId(matchedItem.getId());
            result.setServiceName(matchedItem.getName());
            result.setKeyword(matchedItem.getName());
        } else {
            setDefaultService(result);
        }

        return result;
    }

    private void setDefaultService(SmartMatchResultVO result) {
        LambdaQueryWrapper<ServiceItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceItem::getStatus, 1);
        List<ServiceItem> allItems = serviceItemMapper.selectList(queryWrapper);
        if (!allItems.isEmpty()) {
            ServiceItem defaultItem = allItems.get(0);
            result.setServiceId(defaultItem.getId());
            result.setServiceName(defaultItem.getName());
            result.setKeyword(defaultItem.getName());
        }
    }
    @Resource
    private ChatMemory chatMemory;

    @Override
    public Flux<String> chatAssistantStream(String sessionId, String message) {
        if ("YOUR_API_KEY".equals(apiKey) || StringUtils.isBlank(apiKey)) {
            return Flux.just("对不起，AI服务目前未配置，请联系管理员配置API_KEY。");
        }

        try {
            String systemText = "你是一个名为『家政小智』的智慧家政平台专属AI服务助手。你的任务是热情、专业地解答用户关于本平台系统的疑问。" +
                    "平台核心功能包括：普通家政服务一键预约、保洁/清洗等分类服务展示、特色『多模态智能一键预约』（用户可输入大段文本或上传脏乱房间照片，系统自动识别匹配服务类型和数量）等。" +
                    "你的回答应该简明扼要、语气亲切自然，如果用户问及非家政或本平台之外的话题，请委婉地将其引导回平台服务上。";

            String sid = StringUtils.isNotBlank(sessionId) ? sessionId : "default-session";
            
            return chatClient.prompt()
                    .system(systemText)
                    .user(StringUtils.isNotBlank(message) ? message : "你好")
                    .advisors(new MessageChatMemoryAdvisor(chatMemory, sid, 10))
                    .stream().content();
        } catch (Exception e) {
            log.error("AI 助手调用异常", e);
            return Flux.just("小智遇到了一点网络波动，请稍后再试一次哦~");
        }
    }

    @Resource
    private com.itxc.housekeepbackend.mapper.AiChatSessionMapper sessionMapper;

    @Resource
    private com.itxc.housekeepbackend.mapper.AiChatMessageMapper messageMapper;

    @Override
    public List<com.itxc.housekeepbackend.model.vo.AiChatSessionVO> listChatSessions() {
        Long userId = 0L;
        try {
            User loginUser = userService.getLoginUser();
            if (loginUser != null) {
                userId = loginUser.getId();
            }
        } catch (Exception e) {
            // ignore
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.itxc.housekeepbackend.model.entity.AiChatSession> qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        qw.eq(com.itxc.housekeepbackend.model.entity.AiChatSession::getUserId, userId);
        qw.orderByDesc(com.itxc.housekeepbackend.model.entity.AiChatSession::getUpdateTime);
        
        List<com.itxc.housekeepbackend.model.entity.AiChatSession> dbList = sessionMapper.selectList(qw);
        List<com.itxc.housekeepbackend.model.vo.AiChatSessionVO> result = new java.util.ArrayList<>();
        for (com.itxc.housekeepbackend.model.entity.AiChatSession s : dbList) {
            com.itxc.housekeepbackend.model.vo.AiChatSessionVO vo = new com.itxc.housekeepbackend.model.vo.AiChatSessionVO();
            vo.setSessionId(s.getSessionId());
            vo.setTitle(s.getTitle());
            vo.setCreateTime(s.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<com.itxc.housekeepbackend.model.vo.AiChatMessageVO> listChatMessages(String sessionId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.itxc.housekeepbackend.model.entity.AiChatMessage> qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        qw.eq(com.itxc.housekeepbackend.model.entity.AiChatMessage::getSessionId, sessionId);
        qw.orderByAsc(com.itxc.housekeepbackend.model.entity.AiChatMessage::getId);
        
        List<com.itxc.housekeepbackend.model.entity.AiChatMessage> dbList = messageMapper.selectList(qw);
        List<com.itxc.housekeepbackend.model.vo.AiChatMessageVO> result = new java.util.ArrayList<>();
        for (com.itxc.housekeepbackend.model.entity.AiChatMessage msg : dbList) {
            com.itxc.housekeepbackend.model.vo.AiChatMessageVO vo = new com.itxc.housekeepbackend.model.vo.AiChatMessageVO();
            vo.setRole(msg.getRole());
            vo.setContent(msg.getContent());
            result.add(vo);
        }
        return result;
    }
    
    @Override
    public String chatAssistant(String message) {
        if ("YOUR_API_KEY".equals(apiKey) || StringUtils.isBlank(apiKey)) {
            return "对不起，AI服务目前未配置，请联系管理员配置API_KEY。";
        }

        try {
            String systemText = "你是一个名为『家政小智』的智慧家政平台专属AI服务助手。你的任务是热情、专业地解答用户关于本平台系统的疑问。" +
                    "平台核心功能包括：普通家政服务一键预约、保洁/清洗等分类服务展示、特色『多模态智能一键预约』（用户可输入大段文本或上传脏乱房间照片，系统自动识别匹配服务类型和数量）等。" +
                    "你的回答应该简明扼要、语气亲切自然，如果用户问及非家政或本平台之外的话题，请委婉地将其引导回平台服务上。";

            org.springframework.ai.chat.messages.Message systemMessage = new org.springframework.ai.chat.messages.SystemMessage(systemText);
            UserMessage userMessage = new UserMessage(StringUtils.isNotBlank(message) ? message : "你好");
            
            org.springframework.ai.chat.prompt.Prompt prompt = new org.springframework.ai.chat.prompt.Prompt(List.of(systemMessage, userMessage));
            
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            log.error("AI 助手调用异常", e);
            return "小智遇到了一点网络波动，请稍后再试一次哦~";
        }
    }


}
