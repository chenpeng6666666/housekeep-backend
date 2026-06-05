package com.itxc.housekeepbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.dto.order.BatchDispatchDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderQueryDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.entity.Order;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.BatchDispatchResultVO;
import com.itxc.housekeepbackend.model.vo.OrderVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.OrderService;
import com.itxc.housekeepbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.itxc.housekeepbackend.constant.UserConstant.ADMIN;
import static com.itxc.housekeepbackend.constant.UserConstant.COMPANY;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private UserService userService;

    @Resource
    private CompanyEmployeeService companyEmployeeService;

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
    public BaseResponse<Boolean> cancelOrder(@PathVariable Long id) {
        return ResultUtils.success(orderService.cancelOrder(id));
    }

    /**
     * 企业端：分页查询分配给本企业的订单
     */
    @GetMapping("/company/page")
    @RequireAuth(COMPANY)
    public BaseResponse<Page<Order>> getCompanyOrderPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status
            ) {
        // 1. 解析出当前登录的企业管理员所在的 companyId
        CompanyEmployee employee = companyEmployeeService.getLoginEmp();

        // 2. 构建查询条件
        Page<Order> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        // 核心过滤：只查属于自己企业的订单
        wrapper.eq(Order::getCompanyId, employee.getCompanyId());

        // 可选：按状态过滤 (例如只看 1-待上门 的订单)
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        // 按派单时间倒序排
        wrapper.orderByDesc(Order::getUpdateTime);

        return ResultUtils.success(orderService.page(page, wrapper));
    }


    @GetMapping("/page")
    @RequireAuth(ADMIN)
    public BaseResponse<Page<OrderVO>> listOrderByPage(OrderQueryDTO orderQueryDTO) {
        // 参数校验
        log.info("orderQueryDTO: {}", orderQueryDTO.toString());
        Page<OrderVO> orderPage = orderService.pageAll(orderQueryDTO);

        return ResultUtils.success(orderPage);
    }

    @PostMapping("/batch-dispatch")
    @RequireAuth(ADMIN)
    public BaseResponse<BatchDispatchResultVO> batchDispatch(@RequestBody BatchDispatchDTO batchDispatchDTO) {
        log.info("batchDispatchDTO: {}", batchDispatchDTO.toString());
        BatchDispatchResultVO result = orderService.batchAutoDispatch(batchDispatchDTO);
        return ResultUtils.success(result);
    }




}
