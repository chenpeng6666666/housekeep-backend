package com.itxc.housekeepbackend.memory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itxc.housekeepbackend.mapper.AiChatMessageMapper;
import com.itxc.housekeepbackend.mapper.AiChatSessionMapper;
import com.itxc.housekeepbackend.model.entity.AiChatMessage;
import com.itxc.housekeepbackend.model.entity.AiChatSession;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.service.UserService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class MysqlChatMemory implements ChatMemory {

    @Resource
    private AiChatMessageMapper messageMapper;

    @Resource
    private AiChatSessionMapper sessionMapper;

    @Resource
    private UserService userService;



    @Override
    public void add(String conversationId, List<Message> messages) {
        // 获取当前登录用户，如果获取不到默认为 0
        Long userId = 0L;
        try {
            User loginUser = userService.getLoginUser();
            if (loginUser != null) {
                userId = loginUser.getId();
            }
        } catch (Exception e) {
            // 获取不到忽略
        }

        LambdaQueryWrapper<AiChatSession> sessionQw = new LambdaQueryWrapper<>();
        sessionQw.eq(AiChatSession::getSessionId, conversationId);
        AiChatSession session = sessionMapper.selectOne(sessionQw);
        
        if (session == null) {
            session = new AiChatSession();
            session.setSessionId(conversationId);
            session.setUserId(userId);
            session.setTitle("新会话");
            sessionMapper.insert(session);
        }

        // 保存消息
        for (Message msg : messages) {
            AiChatMessage dbMsg = new AiChatMessage();
            dbMsg.setSessionId(conversationId);
            
            if (msg instanceof UserMessage) {
                dbMsg.setRole("user");
                // 更新会话标题
                if ("新会话".equals(session.getTitle()) || session.getTitle() == null) {
                    String title = msg.getContent();
                    if (title != null) {
                        title = title.length() > 20 ? title.substring(0, 20) : title;
                        session.setTitle(title);
                        sessionMapper.updateById(session);
                    }
                }
            } else if (msg instanceof AssistantMessage) {
                dbMsg.setRole("ai");
            } else if (msg instanceof SystemMessage) {
                dbMsg.setRole("system");
            } else {
                dbMsg.setRole(msg.getMessageType().getValue());
            }
            dbMsg.setContent(msg.getContent());
            messageMapper.insert(dbMsg);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        LambdaQueryWrapper<AiChatMessage> qw = new LambdaQueryWrapper<>();
        qw.eq(AiChatMessage::getSessionId, conversationId);
        qw.orderByDesc(AiChatMessage::getId);
        // lastN 一般用于限制携带上下文条数
        qw.last("limit " + lastN);
        List<AiChatMessage> dbList = messageMapper.selectList(qw);

        List<Message> results = new ArrayList<>();
        // 倒序回正
        for (int i = dbList.size() - 1; i >= 0; i--) {
            AiChatMessage dbMsg = dbList.get(i);
            if ("user".equals(dbMsg.getRole())) {
                results.add(new UserMessage(dbMsg.getContent()));
            } else if ("ai".equals(dbMsg.getRole()) || "assistant".equals(dbMsg.getRole())) {
                results.add(new AssistantMessage(dbMsg.getContent()));
            } else if ("system".equals(dbMsg.getRole())) {
                results.add(new SystemMessage(dbMsg.getContent()));
            }
        }
        return results;
    }

    @Override
    public void clear(String conversationId) {
        LambdaQueryWrapper<AiChatMessage> qw = new LambdaQueryWrapper<>();
        qw.eq(AiChatMessage::getSessionId, conversationId);
        messageMapper.delete(qw);
        
        LambdaQueryWrapper<AiChatSession> sessionQw = new LambdaQueryWrapper<>();
        sessionQw.eq(AiChatSession::getSessionId, conversationId);
        sessionMapper.delete(sessionQw);
    }
}
