package com.itxc.housekeepbackend.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.ServiceItemMapper;
import com.itxc.housekeepbackend.mapper.UserMapper;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.*;
import com.itxc.housekeepbackend.model.vo.OrderVO;
import com.itxc.housekeepbackend.service.EmployeeScheduleService;
import com.itxc.housekeepbackend.service.OrderService;
import com.itxc.housekeepbackend.mapper.OrderMapper;
import com.itxc.housekeepbackend.service.ServiceItemService;
import com.itxc.housekeepbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【order(服务预约订单表)】的数据库操作Service实现
* @createDate 2026-06-01 21:10:37
*/
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
    implements OrderService{

    @Resource
    private ServiceItemMapper serviceItemMapper;

    @Resource
    private UserService userService;

    @Resource
    private ServiceItemService serviceItemService;

    @Resource
    private DispatchEngineServiceImpl dispatchEngine;


    @Resource
    private EmployeeScheduleService employeeScheduleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitOrder(OrderSubmitDTO dto, Long userId) {
        // 1. 校验服务项目是否存在且已上架
        ServiceItem serviceItem = serviceItemMapper.selectById(dto.getServiceId());
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

    @Override
    public Page<OrderVO> page(Long id, Integer current, Integer pageSize, Integer status) {
        // 1. 参数校验
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NO_AUTH_ERROR, "用户不存在");

        // 2. 查询原始订单分页数据
        Page<Order> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, id);
        queryWrapper.eq(status != null, Order::getStatus, status);
        queryWrapper.orderByDesc(Order::getCreateTime);
        Page<Order> orderPage = this.page(page, queryWrapper);

        // 3. 准备构建返回的 VO 分页对象
        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<Order> records = orderPage.getRecords();

        // 如果当前页没有数据，直接返回空列表
        if (records == null || records.isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        // 🚀 4. 高性能连表替代方案：提取当前页所有 serviceId，批量查询服务表
        List<Long> serviceIds = records.stream()
                .map(Order::getServiceId)
                .distinct()
                .collect(Collectors.toList());

        // 假设你有 serviceItemService 提供批量查询功能
        List<ServiceItem> serviceItems = serviceItemService.listByIds(serviceIds);

        // 转换为 Map 方便按 O(1) 复杂度精准匹配查找
        Map<Long, ServiceItem> serviceMap = serviceItems.stream()
                .collect(Collectors.toMap(ServiceItem::getId, item -> item));

        // 5. 将 Order 映射为 OrderVO，并组装服务信息
        List<OrderVO> voList = records.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo); // 拷贝基础属性

            // 组装扩展属性
            ServiceItem serviceItem = serviceMap.get(order.getServiceId());
            if (serviceItem != null) {
                vo.setServiceName(serviceItem.getName());
                vo.setServiceCoverImg(serviceItem.getCoverImg());
                vo.setServiceDescription(serviceItem.getDescription());
            } else {
                vo.setServiceName("未知服务项目"); // 兜底处理已删除服务的情况
            }
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public QueryWrapper<Order> getQueryWrapper(Order order) {
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        if (order == null) {
            return queryWrapper;
        }
        Long userId = order.getUserId();
        String orderNo = order.getOrderNo();
        Long serviceId = order.getServiceId();
        Integer status = order.getStatus();
        String remark = order.getRemark();

        queryWrapper.eq(userId != null, "user_id", userId);
        queryWrapper.like(orderNo != null && !orderNo.isEmpty(), "order_no", orderNo);
        queryWrapper.eq(serviceId != null, "service_id", serviceId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.like(remark != null && !remark.isEmpty(), "remark", remark);
        queryWrapper.orderByDesc("create_time");

        return queryWrapper;
    }

    @Override
    public Boolean cancelOrder(Long id) {
        User loginUser = userService.getLoginUser();
        // 判断当前登录用户是否为该订单的创建者
        Order order = this.getById(id);
        ThrowUtils.throwIf(!order.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限取消该订单");
        ThrowUtils.throwIf(order.getStatus() != 0, ErrorCode.PARAMS_ERROR, "订单已取消或不存在");
        return this.updateById(order);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processSingleOrderDispatch(Order order) {
        // 1. 调用派单匹配引擎，假设标品保洁或清洗服务预估需要 3 小时
        int estimatedHours = 3;
        List<CompanyEmployee> matchedWorkers = dispatchEngine.findMatchWorkers(order, estimatedHours);

        // 如果该订单没有匹配到符合技能、星级且有空闲档期的阿姨，直接返回 false
        if (matchedWorkers.isEmpty()) {
            return false;
        }

        // 2. 算法择优原则：匹配列表已经由高分到低分排好序，直接取出排在第一位的最优质阿姨
        CompanyEmployee bestWorker = matchedWorkers.get(0);

        // 3. 锁定订单状态：双重检查，确保更新状态时采用行锁/乐观锁机制
        order.setStatus(1); // 状态变更为：1-已接单待上门 (或已派单)
        // 实际项目里推荐在这里加上分布式锁，或者更新时增加 eq("status", 0) 的判定条件
        boolean updateResult = this.updateById(order);
        if (!updateResult) {
            return false;
        }

        // 4. 写入员工排班占用表：阻止该阿姨在这段时间内再接别的订单
        EmployeeSchedule schedule = new EmployeeSchedule();
        schedule.setEmployeeId(bestWorker.getId());
        schedule.setOrderNo(order.getOrderNo());
        schedule.setStartTime(order.getServiceTime());
        schedule.setEndTime(DateUtil.offsetHour(order.getServiceTime(), estimatedHours));

        employeeScheduleService.save(schedule);

        // 5. 扩展：如果是商业系统，此处可以调用第三方短信或 WebSocket 推送接口
        // smsUtils.sendSms(bestWorker.getPhone(), "您有一笔新的上门工单，请及时在员工APP查收...");


        return true;
    }


}




