package com.itxc.housekeepbackend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.itxc.housekeepbackend.mapper.ServiceItemMapper;
import com.itxc.housekeepbackend.model.dto.SmartBookingRequestDTO;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.model.vo.SmartMatchResultVO;
import com.itxc.housekeepbackend.service.SmartBookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import jakarta.annotation.Resource;
import java.net.URL;
import java.util.List;

@Service
@Slf4j
public class SmartBookingServiceImpl implements SmartBookingService {

    @Resource
    private ServiceItemMapper serviceItemMapper;

    private final ChatClient chatClient;

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
}
