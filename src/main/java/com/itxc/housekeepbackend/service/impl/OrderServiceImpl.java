package com.itxc.housekeepbackend.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.Order;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.service.OrderService;
import com.itxc.housekeepbackend.mapper.OrderMapper;
import com.itxc.housekeepbackend.service.ServiceItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
* @author Lenovo
* @description 针对表【order(服务预约订单表)】的数据库操作Service实现
* @createDate 2026-06-01 21:10:37
*/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService{


    @Resource
    private ServiceItemService serviceItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitOrder(OrderSubmitDTO dto, Long userId) {
        // 1. 校验服务项目是否存在且已上架
        ServiceItem serviceItem = serviceItemService.getById(dto.getServiceId());
        if (serviceItem == null || serviceItem.getStatus() == 0) {
            throw new RuntimeException("该服务项目不存在或已下架");
        }

        // 2. 核心防御：后端重新计算真实总价 (单价 * 数量)
        BigDecimal realTotalAmount = serviceItem.getGuidancePrice().multiply(new BigDecimal(dto.getQuantity()));

        // 3. 生成业务订单号 (规则：ORD + 年月日时分秒 + 4位随机数)
        String orderNo = "ORD" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN) + RandomUtil.randomNumbers(4);

        // 4. 组装订单实体并落库
        Order order = new Order();
        BeanUtils.copyProperties(dto, order);
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setTotalAmount(realTotalAmount); // 强制使用后端计算的价格
        order.setStatus(0); // 初始状态：0-待派单 (或待付款，这里假设直接进入派单池)

        // 处理星级要求默认值
        if (dto.getRequireScore() == null) {
            order.setRequireScore(BigDecimal.ZERO);
        }

        this.save(order);

        // 返回订单号给前端
        return orderNo;
    }
}




