package com.itxc.housekeepbackend.agent;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itxc.housekeepbackend.mapper.ServiceItemMapper;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.model.vo.AiAssistantResponseVO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HousekeepingAssistantTools {

    @Resource
    private ServiceItemMapper serviceItemMapper;

    @Tool(
            name = "search_housekeeping_services",
            description = "根据用户输入的家政服务关键词查询平台已上架服务。用于用户咨询某类服务、询价、比较服务，或准备预约下单前查找合适服务。参数 keyword 应是服务关键词，例如 保洁、开荒、擦玻璃、家电清洗、月嫂。返回 JSON 字符串，包含最多 5 个服务的 id、名称、价格、单位、预计耗时和服务描述。"
    )
    public String searchServices(
            @ToolParam(description = "服务关键词，例如 保洁、开荒、擦玻璃、家电清洗、月嫂。不能为空。") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return error("validation_error", "keyword 不能为空，请根据用户需求提取一个家政服务关键词。");
        }

        List<ServiceItem> list = serviceItemMapper.selectList(new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getStatus, 1)
                .like(ServiceItem::getName, keyword.trim())
                .orderByAsc(ServiceItem::getSort)
                .last("limit 5"));

        if (list == null || list.isEmpty()) {
            return error("not_found", "没有查询到匹配服务。请换用更宽泛的关键词，例如 保洁、清洗、月嫂。");
        }

        List<AiAssistantResponseVO.ServiceCard> cards = list.stream()
                .map(this::toServiceCard)
                .collect(Collectors.toList());
        return JSONUtil.toJsonStr(cards);
    }

    @Tool(
            name = "get_housekeeping_service_detail",
            description = "根据平台服务 ID 查询单个家政服务详情。用于用户追问服务内容、价格、计费单位、预计耗时、服务标准等。参数 serviceId 必须来自 search_housekeeping_services 的结果。返回 JSON 字符串。"
    )
    public String getServiceDetail(
            @ToolParam(description = "平台服务 ID，必须是 search_housekeeping_services 返回的 serviceId。") Long serviceId) {
        if (serviceId == null) {
            return error("validation_error", "serviceId 不能为空。");
        }
        ServiceItem item = serviceItemMapper.selectById(serviceId);
        if (item == null || item.getStatus() == null || item.getStatus() != 1) {
            return error("not_found", "服务不存在或已下架，请重新搜索服务。");
        }
        return JSONUtil.toJsonStr(toServiceCard(item));
    }

    @Tool(
            name = "create_order_draft_card",
            description = "为当前用户生成订单草稿卡片，但不创建真实订单。仅当用户明确表达预约、下单、安排上门、需要阿姨/家政服务等意图时使用。参数 serviceId 必须来自服务查询结果；quantity 为服务数量，不能小于 1；remark 是用户原始需求整理后的备注。返回 JSON 字符串，前端会展示为可点击确认的订单卡片。"
    )
    public String createOrderDraftCard(
            @ToolParam(description = "平台服务 ID，必须来自 search_housekeeping_services 或 get_housekeeping_service_detail 的结果。") Long serviceId,
            @ToolParam(description = "预约服务数量。无法判断时填 1，不能小于 1。") Integer quantity,
            @ToolParam(required = false, description = "订单备注，整理用户的特殊要求，例如面积、房型、宠物、工具偏好、重点清洁区域。") String remark) {
        if (serviceId == null) {
            return error("validation_error", "serviceId 不能为空，必须先查询服务。");
        }

        ServiceItem item = serviceItemMapper.selectById(serviceId);
        if (item == null || item.getStatus() == null || item.getStatus() != 1) {
            return error("not_found", "服务不存在或已下架，请重新查询服务。");
        }

        int safeQuantity = quantity == null || quantity < 1 ? 1 : quantity;
        AiAssistantResponseVO.OrderDraftCard card = new AiAssistantResponseVO.OrderDraftCard();
        card.setServiceId(item.getId());
        card.setServiceName(item.getName());
        card.setServiceCoverImg(item.getCoverImg());
        card.setServiceDescription(item.getDescription());
        card.setUnit(item.getUnit());
        card.setGuidancePrice(item.getGuidancePrice());
        card.setBaseDuration(item.getBaseDuration());
        card.setQuantity(safeQuantity);
        card.setEstimatedAmount(item.getGuidancePrice().multiply(BigDecimal.valueOf(safeQuantity)));
        card.setRemark(remark);

        return JSONUtil.toJsonStr(card);
    }

    public AiAssistantResponseVO.OrderDraftCard createDraftCardDirect(Long serviceId, Integer quantity, String remark) {
        String json = createOrderDraftCard(serviceId, quantity, remark);
        if (!JSONUtil.isTypeJSON(json) || JSONUtil.parseObj(json).containsKey("error")) {
            return null;
        }
        return JSONUtil.toBean(json, AiAssistantResponseVO.OrderDraftCard.class);
    }

    public List<AiAssistantResponseVO.ServiceCard> searchServicesDirect(String keyword) {
        String json = searchServices(keyword);
        if (!JSONUtil.isTypeJSON(json)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(json), AiAssistantResponseVO.ServiceCard.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private AiAssistantResponseVO.ServiceCard toServiceCard(ServiceItem item) {
        AiAssistantResponseVO.ServiceCard card = new AiAssistantResponseVO.ServiceCard();
        card.setServiceId(item.getId());
        card.setServiceName(item.getName());
        card.setServiceCoverImg(item.getCoverImg());
        card.setServiceDescription(item.getDescription());
        card.setUnit(item.getUnit());
        card.setGuidancePrice(item.getGuidancePrice());
        card.setBaseDuration(item.getBaseDuration());
        return card;
    }

    private String error(String type, String message) {
        return JSONUtil.toJsonStr(new ToolError(true, type, message));
    }

    private record ToolError(boolean error, String errorType, String message) {
    }
}
