package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.model.dto.order.BatchDispatchDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderQueryDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itxc.housekeepbackend.model.vo.BatchDispatchResultVO;
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
     *
     * @param order
     * @return
     */
    Wrapper<Order> getQueryWrapper(Order order);

    /**
     * 用户取消订单
     * @param id 订单ID
     * @return 订单取消结果
     */
    Boolean cancelOrder(Long id);

    /**
     * 批量订单派单
     * @param dto 订单派单请求
     * @return 派单结果返回
     */
    BatchDispatchResultVO batchAutoDispatch(BatchDispatchDTO dto);

    /**
     * 所有订单分页查询
     * @param orderQueryDTO 订单分页查询请求
     * @return 订单分页查询结果
     */
    Page<OrderVO> pageAll(OrderQueryDTO orderQueryDTO);
}
