package com.itxc.housekeepbackend.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.*;
import com.itxc.housekeepbackend.model.dto.order.BatchDispatchDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderQueryDTO;
import com.itxc.housekeepbackend.model.dto.order.OrderSubmitDTO;
import com.itxc.housekeepbackend.model.entity.*;
import com.itxc.housekeepbackend.model.vo.BatchDispatchResultVO;
import com.itxc.housekeepbackend.model.vo.CandidateVO;
import com.itxc.housekeepbackend.model.vo.OrderVO;
import com.itxc.housekeepbackend.service.OrderService;
import com.itxc.housekeepbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
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
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Resource
    private CompanyEmployeeMapper companyEmployeeMapper;

    @Resource
    private EmployeeScheduleMapper employeeScheduleMapper;

    @Resource
    private CompanyMapper companyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String submitOrder(OrderSubmitDTO dto, Long userId) {
        // 1. 校验服务项目是否存在且已上架
        ServiceItem serviceItem = serviceItemMapper.selectById(dto.getServiceId());
        if (serviceItem == null || serviceItem.getStatus() == 0) {
            throw new RuntimeException("该服务项目不存在或已下架");
        }

        // 计算总耗时 (分钟)
        int totalMinutes = 0;
        if ("小时".equals(serviceItem.getUnit())) {
            totalMinutes = dto.getQuantity() * 60; // 按小时买，直接乘以60
        } else {
            totalMinutes = Math.toIntExact(dto.getQuantity() * serviceItem.getBaseDuration()); // 按件买，乘基准耗时
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
        order.setEstimatedEndTime(DateUtil.offsetMinute(dto.getServiceTime(), totalMinutes)); // 固化预估结束时间

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
        User user = userMapper.selectById(id);
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

        // 4. 高性能连表替代方案：提取当前页所有 serviceId，批量查询服务表
        List<Long> serviceIds = records.stream()
                .map(Order::getServiceId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询功能
        List<ServiceItem> serviceItems = serviceItemMapper.selectByIds(serviceIds);

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
    public Wrapper<Order> getQueryWrapper(Order order) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        if (order == null) {
            return queryWrapper;
        }
        Long id = order.getId();
        String orderNo = order.getOrderNo();
        Long userId = order.getUserId();
        Long serviceId = order.getServiceId();
        Long companyId = order.getCompanyId();
        Long employeeId = order.getEmployeeId();
        Integer status = order.getStatus();
        String remark = order.getRemark();

        queryWrapper.eq(id != null, Order::getId, id);
        queryWrapper.like(orderNo != null && !orderNo.isEmpty(), Order::getOrderNo, orderNo);
        queryWrapper.eq(userId != null, Order::getUserId, userId);
        queryWrapper.eq(serviceId != null, Order::getServiceId, serviceId);
        queryWrapper.eq(companyId != null, Order::getCompanyId, companyId);
        queryWrapper.eq(employeeId != null, Order::getEmployeeId, employeeId);
        queryWrapper.eq(status != null, Order::getStatus, status);
        queryWrapper.like(remark != null && !remark.isEmpty(), Order::getRemark, remark);
        queryWrapper.orderByDesc(Order::getCreateTime);

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
    public BatchDispatchResultVO batchAutoDispatch(BatchDispatchDTO dto) {
        BatchDispatchResultVO result = new BatchDispatchResultVO();
        List<String> failMessages = new ArrayList<>();
        int successCount = 0;

        // 1. 获取所有需要派发的订单
        List<Order> orders = this.listByIds(dto.getOrderIds());

        // 2. 遍历每一个订单执行智能匹配
        for (Order order : orders) {
            try {
                if (order.getStatus() != 0) {
                    throw new RuntimeException("订单已不在待派单状态");
                }

                // --- 漏斗 1: 技能与星级过滤 ---
                List<CandidateVO> candidates = companyEmployeeMapper.getCandidatesBySkillAndScore(
                        order.getServiceId(), order.getRequireScore());

                if (candidates == null || candidates.isEmpty()) {
                    throw new RuntimeException("全平台无满足该星级或技能的家政员");
                }

                List<Long> candidateIds = candidates.stream().map(CandidateVO::getId).collect(Collectors.toList());

                // --- 漏斗 2: 动态档期冲突检测 ---
                // 注意：由于是在一个 for 循环的事务里，这里每次查询都会带上刚刚上一个订单插入的 schedule 数据，完美避免“脏读撞单”
                List<Long> busyIds = employeeScheduleMapper.getBusyEmployeeIds(
                        order.getServiceTime(), order.getEstimatedEndTime(), candidateIds);

                // --- 漏斗 3: 择优录取 ---
                CandidateVO bestMatch = null;
                for (CandidateVO candidate : candidates) {
                    if (busyIds == null || !busyIds.contains(candidate.getId())) {
                        bestMatch = candidate;
                        break; // 找到评分最高且有空的，立刻跳出
                    }
                }

                if (bestMatch == null) {
                    throw new RuntimeException("符合条件的家政员在此时段均已排满");
                }

                // --- 派单落盘 ---
                order.setCompanyId(bestMatch.getCompanyId());
                order.setEmployeeId(bestMatch.getId());
                order.setStatus(1); // 状态流转: 1-已接单待上门 (或已分派企业)
                this.updateById(order);

                // 立刻写入排班表，这样下一个循环时，这个阿姨这个时间段就被锁住了！
                EmployeeSchedule schedule = new EmployeeSchedule();
                schedule.setEmployeeId(bestMatch.getId());
                schedule.setOrderNo(order.getOrderNo());
                schedule.setStartTime(order.getServiceTime());
                schedule.setEndTime(order.getEstimatedEndTime());
                employeeScheduleMapper.insert(schedule);

                successCount++;

            } catch (Exception e) {
                // 收集失败原因，不中断其他订单的派发
                failMessages.add("单号 " + order.getOrderNo() + " 派发失败: " + e.getMessage());
            }
        }

        result.setSuccessCount(successCount);
        result.setFailCount(orders.size() - successCount);
        result.setFailMessages(failMessages);
        return result;
    }

    @Override
    public Page<OrderVO> pageAll(OrderQueryDTO queryDTO) {
        // 1. 构建分页与基础查询条件
        Page<Order> page = new Page<>(queryDTO.getCurrent(), queryDTO.getPageSize());
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(queryDTO.getOrderNo())) {
            wrapper.like(Order::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        // 预约时间范围过滤
        if (StringUtils.isNotBlank(queryDTO.getStartTime()) && StringUtils.isNotBlank(queryDTO.getEndTime())) {
            wrapper.between(Order::getServiceTime, queryDTO.getStartTime(), queryDTO.getEndTime());
        }

        // 按最新订单排序
        wrapper.orderByDesc(Order::getCreateTime);

        // 2. 执行查询
        Page<Order> orderPage = this.page(page, wrapper);

        // 3. 构建返回的 VO 分页对象
        Page<OrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<Order> records = orderPage.getRecords();
        if (records == null || records.isEmpty()) {
            return voPage;
        }
        // 4. 高性能组装：提取当前页涉及的所关联 ID
        List<Long> serviceIds = records.stream().map(Order::getServiceId).distinct().collect(Collectors.toList());
        List<Long> companyIds = records.stream().map(Order::getCompanyId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> employeeIds = records.stream().map(Order::getEmployeeId).filter(id -> id != null).distinct().collect(Collectors.toList());

        // 批量查询字典数据并转为 Map 便于 O(1) 匹配
        Map<Long, String> serviceMap = serviceItemMapper.selectByIds(serviceIds).stream()
                .collect(Collectors.toMap(ServiceItem::getId, ServiceItem::getName));

        Map<Long, String> companyMap = companyIds.isEmpty() ? java.util.Collections.emptyMap() :
                companyMapper.selectByIds(companyIds).stream()
                        .collect(Collectors.toMap(Company::getId, Company::getCompanyName));

        Map<Long, String> employeeMap = employeeIds.isEmpty() ? java.util.Collections.emptyMap() :
                companyEmployeeMapper.selectByIds(employeeIds).stream()
                        .collect(Collectors.toMap(CompanyEmployee::getId, CompanyEmployee::getRealName));

        // 5. 将 Order 映射为 AdminOrderVO
        List<OrderVO> voList = records.stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);

            // 动态赋值关联名称
            vo.setServiceName(serviceMap.getOrDefault(order.getServiceId(), "未知服务"));
            vo.setCompanyName(companyMap.get(order.getCompanyId()));
            vo.setEmployeeName(employeeMap.get(order.getEmployeeId()));

            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }


}




