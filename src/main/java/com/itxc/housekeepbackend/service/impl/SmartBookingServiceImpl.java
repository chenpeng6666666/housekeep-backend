package com.itxc.housekeepbackend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.itxc.housekeepbackend.agent.HousekeepingAssistantTools;
import com.itxc.housekeepbackend.mapper.AiChatMessageMapper;
import com.itxc.housekeepbackend.mapper.AiChatSessionMapper;
import com.itxc.housekeepbackend.mapper.ServiceItemMapper;
import com.itxc.housekeepbackend.model.dto.SmartBookingRequestDTO;
import com.itxc.housekeepbackend.model.entity.AiChatMessage;
import com.itxc.housekeepbackend.model.entity.AiChatSession;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.AiAssistantResponseVO;
import com.itxc.housekeepbackend.model.vo.AiChatMessageVO;
import com.itxc.housekeepbackend.model.vo.AiChatSessionVO;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SmartBookingServiceImpl implements SmartBookingService {

    @Resource
    private ServiceItemMapper serviceItemMapper;

    private final ChatClient chatClient;

    @Resource
    private UserService userService;

    @Resource
    private HousekeepingAssistantTools housekeepingAssistantTools;

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
                    UserMessage userMessage = UserMessage.builder()
                            .text(userText)
                            .media(List.of(new org.springframework.ai.content.Media(MimeTypeUtils.IMAGE_JPEG, java.net.URI.create(imageUrl))))
                            .build();
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

    private static final String AGENT_STREAM_SYSTEM_PROMPT = """
            你是“家政小智”，智慧家政在线服务平台的 AI 助手。

            你必须严格遵守以下规则：
            1. 绝不编造：你不能编造平台不存在的服务、价格、服务 ID、员工或企业信息。
            2. 严格依赖上下文：系统会在【当前上下文】中自动附带后端数据库查询到的真实服务数据或订单草稿。如果用户询问某项服务，你**只能**基于【当前上下文】中提供的数据进行回答。如果上下文中明确表示“没有命中具体平台服务”或没有提供相关数据，请明确告知用户平台目前不提供该服务，并引导他们询问其他常见服务（如日常保洁、月嫂等）。
            3. 订单引导：如果【当前上下文】中包含“订单草稿”，说明系统已经预生成了预约卡片。请用自然亲切的语言提醒用户查看下方的卡片，并引导他们点击卡片以填写地址和上门时间。
            4. 澄清需求：当用户需求不明确（例如仅说“打扫卫生”）时，请主动追问具体的服务类型、面积或时间偏好。
            5. 输出格式：直接输出自然、简洁、热情的中文回复，适合聊天框展示。绝对不要输出任何 JSON、隐藏标记或 Markdown 代码块。

            记住核心原则：没有在上下文中出现的服务，就是本平台没有的服务，坚决不能无中生有！
            """;

    @Override
    public Flux<String> chatAssistantStream(String sessionId, String message) {
        if (StringUtils.isBlank(message)) {
            return Flux.just("您可以告诉我想咨询哪类家政服务，或直接说想预约什么服务。");
        }

        if ("YOUR_API_KEY".equals(apiKey) || StringUtils.isBlank(apiKey)) {
            return streamFallbackAgent(message);
        }

        try {
            String sid = StringUtils.isNotBlank(sessionId) ? sessionId : "default-session";
            String toolContext = buildStreamToolContext(message);
            String finalSystemPrompt = AGENT_STREAM_SYSTEM_PROMPT + "\n\n【当前上下文】\n" + toolContext;

            Flux<String> contentStream = chatClient.prompt()
                    .system(finalSystemPrompt)
                    .user(message)
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(sid).build())
                    .stream().content();

            String cardPayload = buildStreamCardPayload(message);
            return StringUtils.isNotBlank(cardPayload) ? contentStream.concatWith(Flux.just(cardPayload)) : contentStream;
        } catch (Exception e) {
            log.error("AI 工具流式助手调用异常，降级到本地工具流程", e);
            return streamFallbackAgent(message);
        }
    }

    private Flux<String> streamFallbackAgent(String message) {
        AiAssistantResponseVO response = fallbackAgent(message);
        StringBuilder builder = new StringBuilder(response.getContent() == null ? "" : response.getContent());
        appendCardPayload(builder, response);
        return Flux.fromArray(builder.toString().split("(?<=.)"));
    }

    private String buildStreamToolContext(String message) {
        if (!shouldAttachToolPayload(message)) {
            return "当前没有命中具体平台服务。请按平台家政助手身份，用简洁中文回答用户。";
        }

        AiAssistantResponseVO response = fallbackAgent(message);
        StringBuilder builder = new StringBuilder("以下是后端基于平台数据库查询到的真实服务数据，只能基于这些数据回答，不要编造价格、服务 ID 或不存在的服务：\n");
        if (response.getOrderCard() != null) {
            builder.append("订单草稿：").append(JSONUtil.toJsonStr(response.getOrderCard())).append("\n");
        }
        if (response.getServices() != null && !response.getServices().isEmpty()) {
            builder.append("服务列表：").append(JSONUtil.toJsonStr(response.getServices())).append("\n");
        }
        return builder.toString();
    }

    private String buildStreamCardPayload(String message) {
        if (!shouldAttachToolPayload(message)) {
            return "";
        }
        AiAssistantResponseVO response = fallbackAgent(message);
        StringBuilder builder = new StringBuilder();
        appendCardPayload(builder, response);
        return builder.toString();
    }

    private void appendCardPayload(StringBuilder builder, AiAssistantResponseVO response) {
        if (response.getOrderCard() != null) {
            builder.append("\n[[HOUSEKEEP_ORDER_CARD]]")
                    .append(JSONUtil.toJsonStr(response.getOrderCard()))
                    .append("[[/HOUSEKEEP_ORDER_CARD]]");
        }
        if (response.getServices() != null && !response.getServices().isEmpty()) {
            builder.append("\n[[HOUSEKEEP_SERVICES]]")
                    .append(JSONUtil.toJsonStr(response.getServices()))
                    .append("[[/HOUSEKEEP_SERVICES]]");
        }
    }

    private boolean shouldAttachToolPayload(String message) {
        return looksLikeOrderIntent(message) || containsServiceKeyword(message);
    }

    private static final String AGENT_SYSTEM_PROMPT = """
            你是“家政小智”，智慧家政在线服务平台的后端智能体。

            你必须遵守：
            1. 当用户咨询服务、价格、服务标准、适合哪类服务时，必须调用 search_housekeeping_services 或 get_housekeeping_service_detail 获取平台真实服务数据，再回答。
            2. 当用户表达预约、下单、安排上门、找阿姨、需要家政服务等明确交易意图时，必须先查询服务，再调用 create_order_draft_card 生成订单草稿卡片。
            3. 订单草稿卡片只是预生成信息，不能代表用户下单。必须提醒用户下一步点击卡片确认并填写地址、上门时间。
            4. 不要编造平台不存在的服务、价格、服务 ID、员工或企业信息。
            5. 用户需求不明确时，先追问服务类型、数量、面积或时间偏好。

            最终回复必须只输出 JSON，不要输出 Markdown，不要包裹代码块：
            {
              "type": "TEXT | SERVICE_INFO | ORDER_DRAFT",
              "content": "给用户看的中文回复",
              "orderCard": null 或订单草稿对象,
              "services": null 或服务卡片数组
            }
            """;

    @Override
    public AiAssistantResponseVO chatAgent(String sessionId, String message) {
        if (StringUtils.isBlank(message)) {
            return textResponse("您可以告诉我想咨询哪类家政服务，或直接说想预约什么服务。");
        }

        if ("YOUR_API_KEY".equals(apiKey) || StringUtils.isBlank(apiKey)) {
            return fallbackAgent(message);
        }

        try {
            String sid = StringUtils.isNotBlank(sessionId) ? sessionId : "default-session";
            AiAssistantResponseVO response = chatClient.prompt()
                    .system(AGENT_SYSTEM_PROMPT)
                    .user(message)
                    .tools(housekeepingAssistantTools)
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(sid).build())
                    .call()
                    .entity(AiAssistantResponseVO.class);

            if (response == null || StringUtils.isBlank(response.getContent())) {
                return textResponse("我已经收到您的需求，但暂时没有整理出可展示的服务信息，请换一种描述再试。");
            }
            if (StringUtils.isBlank(response.getType())) {
                response.setType(response.getOrderCard() != null ? "ORDER_DRAFT" : "TEXT");
            }
            return response;
        } catch (Exception e) {
            log.error("AI 工具助手调用异常，降级到本地工具流程", e);
            return fallbackAgent(message);
        }
    }

    private AiAssistantResponseVO fallbackAgent(String message) {
        String keyword = guessServiceKeyword(message);
        List<AiAssistantResponseVO.ServiceCard> services = housekeepingAssistantTools.searchServicesDirect(keyword);
        if (services == null || services.isEmpty()) {
            return textResponse("我暂时没有匹配到合适的服务。请补充服务类型，例如保洁、开荒、擦玻璃、家电清洗或月嫂。");
        }

        AiAssistantResponseVO response = new AiAssistantResponseVO();
        response.setServices(services);

        if (looksLikeOrderIntent(message)) {
            AiAssistantResponseVO.ServiceCard first = services.get(0);
            AiAssistantResponseVO.OrderDraftCard card = housekeepingAssistantTools.createDraftCardDirect(
                    first.getServiceId(), guessQuantity(message), message);
            response.setType("ORDER_DRAFT");
            response.setContent("我已根据您的描述匹配到平台服务，并生成了订单草稿卡片。请点击卡片确认后填写地址和上门时间。");
            response.setOrderCard(card);
        } else {
            response.setType("SERVICE_INFO");
            AiAssistantResponseVO.ServiceCard first = services.get(0);
            String description = StringUtils.isNotBlank(first.getServiceDescription()) ? first.getServiceDescription() : "您也可以继续告诉我是否需要预约。";
            response.setContent(String.format("为您查询到“%s”。平台指导价为 %s 元/%s。%s",
                    first.getServiceName(), first.getGuidancePrice(), first.getUnit(), description));
        }
        return response;
    }

    private AiAssistantResponseVO textResponse(String content) {
        AiAssistantResponseVO response = new AiAssistantResponseVO();
        response.setType("TEXT");
        response.setContent(content);
        return response;
    }

    private boolean looksLikeOrderIntent(String message) {
        return message.contains("预约") || message.contains("下单") || message.contains("安排")
                || message.contains("上门") || message.contains("找阿姨") || message.contains("需要");
    }

    private boolean containsServiceKeyword(String message) {
        return message.contains("开荒") || message.contains("擦玻璃") || message.contains("家电")
                || message.contains("油烟机") || message.contains("空调") || message.contains("月嫂")
                || message.contains("育儿") || message.contains("保姆") || message.contains("护工")
                || message.contains("收纳") || message.contains("做饭") || message.contains("保洁")
                || message.contains("清洁") || message.contains("维修");
    }

    private String guessServiceKeyword(String message) {
        String[] keywords = {"开荒", "擦玻璃", "家电", "油烟机", "空调", "月嫂", "育儿", "保姆", "护工", "收纳", "做饭", "保洁", "清洁", "维修"};
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return keyword;
            }
        }
        return "保洁";
    }

    private Integer guessQuantity(String message) {
        Matcher matcher = Pattern.compile("(\\d+)\\s*(小时|次|平|平方|平方米|台)").matcher(message);
        if (matcher.find()) {
            try {
                return Math.max(1, Integer.parseInt(matcher.group(1)));
            } catch (NumberFormatException ignored) {
                return 1;
            }
        }
        return 1;
    }

    @Resource
    private AiChatSessionMapper sessionMapper;

    @Resource
    private AiChatMessageMapper messageMapper;

    @Override
    public List<AiChatSessionVO> listChatSessions() {
        Long userId = 0L;
        try {
            User loginUser = userService.getLoginUser();
            if (loginUser != null) {
                userId = loginUser.getId();
            }
        } catch (Exception e) {
            // ignore
        }

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatSession> qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        qw.eq(AiChatSession::getUserId, userId);
        qw.orderByDesc(AiChatSession::getUpdateTime);
        
        List<AiChatSession> dbList = sessionMapper.selectList(qw);
        List<AiChatSessionVO> result = new java.util.ArrayList<>();
        for (AiChatSession s : dbList) {
            com.itxc.housekeepbackend.model.vo.AiChatSessionVO vo = new com.itxc.housekeepbackend.model.vo.AiChatSessionVO();
            vo.setSessionId(s.getSessionId());
            vo.setTitle(s.getTitle());
            vo.setCreateTime(s.getCreateTime());
            vo.setUpdateTime(s.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<com.itxc.housekeepbackend.model.vo.AiChatMessageVO> listChatMessages(String sessionId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiChatMessage> qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        qw.eq(AiChatMessage::getSessionId, sessionId);
        qw.orderByAsc(AiChatMessage::getId);
        List<AiChatMessage> dbList = messageMapper.selectList(qw);
        List<AiChatMessageVO> result = new java.util.ArrayList<>();
        for (AiChatMessage msg : dbList) {
            AiChatMessageVO vo = new AiChatMessageVO();
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
