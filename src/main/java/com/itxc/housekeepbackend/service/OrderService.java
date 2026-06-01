package com.itxc.housekeepbackend.service;

import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Lenovo
* @description 针对表【order(服务预约订单表)】的数据库操作Service
* @createDate 2026-06-01 21:10:37
*/
public interface OrderService extends IService<Order> {

    /**
     * 用户提交订单
     * @param dto
     * @param userId
     * @return
     */
    String submitOrder(OrderSubmitDTO dto, Long userId);
}
