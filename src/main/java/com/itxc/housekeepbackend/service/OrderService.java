package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itxc.housekeepbackend.model.vo.OrderVO;

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

    /**
     * 用户订单分页查询
     * @param id 用户id
     * @param current 页号
     * @param pageSize 页大小
      * @param status 订单状态 0-待派单 1-待接单 2-待服务 3-待评价 4-已完成 5-已取消
     * @return 分页结果
     */
    Page<OrderVO> page(Long id, Integer current, Integer pageSize, Integer status);

    /**
     * 封装订单查询条件
     * @param order
     * @return
     */
    QueryWrapper getQueryWrapper(Order order);

    /**
     * 用户取消订单
     * @param id
     * @return
     */
    Boolean cancelOrder(Long id);

    /**
     * 处理单个订单派单
     * @param order
     * @return
     */
    boolean processSingleOrderDispatch(Order order);
}
