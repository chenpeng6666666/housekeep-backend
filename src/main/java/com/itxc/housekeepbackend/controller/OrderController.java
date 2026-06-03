package com.itxc.housekeepbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.Order;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.OrderVO;
import com.itxc.housekeepbackend.service.OrderService;
import com.itxc.housekeepbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private UserService userService;

    /**
     * C端用户提交服务预约订单
     */
    @PostMapping("/submit")
    @RequireAuth
    public BaseResponse<String> submitOrder(@RequestBody @Validated OrderSubmitDTO dto) {
        User loginUser = userService.getLoginUser();
        // 调用 Service 核心下单逻辑，返回订单号
        String orderNo = orderService.submitOrder(dto, loginUser.getId());
        return ResultUtils.success(orderNo);
    }

    /**
     * 获取用户订单分页
     */
    @GetMapping("/myPage")
    @RequireAuth
    public BaseResponse<Page<OrderVO>> getMyPage(@RequestParam(required = false, defaultValue = "1") Integer current,
                                                 @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                                                 Integer status) {
        User loginUser = userService.getLoginUser();
        return ResultUtils.success(orderService.page(loginUser.getId(), current, pageSize,status));
    }

    /**
     * 用户取消订单
     */
    @PutMapping("/cancel/{id}")
    @RequireAuth
    public BaseResponse<Boolean> cancelOrder(@PathVariable Long id) {
        return ResultUtils.success(orderService.cancelOrder(id));
    }







}
