package com.itxc.housekeepbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.dto.order.BatchDispatchDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderQueryDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderRreassignEmpDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.entity.Order;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.BatchDispatchResultVO;
import com.itxc.housekeepbackend.model.vo.CandidateVO;
import com.itxc.housekeepbackend.model.vo.OrderVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.OrderService;
import com.itxc.housekeepbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.List;

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
     * C端: 用户提交服务预约订单
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
        return ResultUtils.success(orderService.page(loginUser.getId(), current, pageSize, status));
    }

    /**
     * C端: 用户取消订单
     */
    @PutMapping("/cancel/{id}")
    public BaseResponse<Boolean> cancelOrder(@PathVariable Long id) {
        return ResultUtils.success(orderService.cancelOrder(id));
    }

    /**
     * 企业端：分页查询分配给本企业的订单
     */
    @GetMapping("/companyPage")
    @RequireAuth(COMPANY)
    public BaseResponse<Page<OrderVO>> getCompanyOrderPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status
    ) {
        // 1. 解析出当前登录的企业管理员所在的 companyId
        CompanyEmployee employee = companyEmployeeService.getLoginEmp();

        // 2. 调用封装好的带有VO关联查询的方法
        Page<OrderVO> pageVO = orderService.pageCompany(employee.getCompanyId(), current, pageSize, status);

        return ResultUtils.success(pageVO);
    }

    /**
     * 企业端：获取某订单可替换的员工列表
     */
    @GetMapping("/eligible-employees/{orderId}")
    @RequireAuth(COMPANY)
    public BaseResponse<List<CandidateVO>> getEligibleEmployees(@PathVariable Long orderId) {
        log.info("待替换订单 orderId: {}", orderId);
        List<CandidateVO> list = orderService.getEligibleEmp(orderId);
        // 3 返回结果
        return ResultUtils.success(list);
    }

    /**
     * 企业确认接单
     */
    @PutMapping("/confirm/{orderId}")
    @RequireAuth(COMPANY)
    public BaseResponse<Boolean> acceptOrder(@PathVariable Long orderId){
        log.info("企业接单 orderId: {}", orderId);
        return ResultUtils.success(orderService.acceptOrder(orderId));
    }

    /**
     * 订单重新分配员工
     */
    @PutMapping("/reassignEmp")
    @RequireAuth(COMPANY)
    public BaseResponse<Boolean> reassignEmp(@RequestBody OrderRreassignEmpDTO dto) {
        log.info("dto: {}", dto.toString());
        Boolean b = orderService.reassignEmp(dto);
        return ResultUtils.success(b);
    }


    /**
     * 平台订单分页查询
     */
    @GetMapping("/page")
    @RequireAuth(ADMIN)
    public BaseResponse<Page<OrderVO>> listOrderByPage(OrderQueryDTO orderQueryDTO) {
        // 参数校验
        log.info("orderQueryDTO: {}", orderQueryDTO.toString());
        Page<OrderVO> orderPage = orderService.pageAll(orderQueryDTO);

        return ResultUtils.success(orderPage);
    }

    /**
     * 批量派单
     */
    @PostMapping("/batch-dispatch")
    @RequireAuth(ADMIN)
    public BaseResponse<BatchDispatchResultVO> batchDispatch(@RequestBody BatchDispatchDTO batchDispatchDTO) {
        log.info("batchDispatchDTO: {}", batchDispatchDTO.toString());
        BatchDispatchResultVO result = orderService.batchAutoDispatch(batchDispatchDTO);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID查询订单详情
     */
    @GetMapping("/detail/{id}")
    public BaseResponse<OrderVO> getOrderDetail(@PathVariable Long id) {
        OrderVO detail = orderService.getOrderDetail(id);
        return ResultUtils.success(detail);
    }

}
