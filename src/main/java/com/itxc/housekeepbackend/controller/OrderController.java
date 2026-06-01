package com.itxc.housekeepbackend.controller;

import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.service.OrderService;
import com.itxc.housekeepbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

}
