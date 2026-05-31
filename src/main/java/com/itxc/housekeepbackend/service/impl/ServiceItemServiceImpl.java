package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.model.dto.serviceItem.ServiceItemQueryRequest;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.service.ServiceItemService;
import com.itxc.housekeepbackend.mapper.ServiceItemMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【service_item(平台标准服务项目表)】的数据库操作Service实现
* @createDate 2026-05-31 12:37:35
*/
@Service
public class ServiceItemServiceImpl extends ServiceImpl<ServiceItemMapper, ServiceItem>
    implements ServiceItemService{

    @Override
    public Page<ServiceItem> pageQuery(ServiceItemQueryRequest request) {
        Page<ServiceItem> page = new Page<>(request.getCurrent(), request.getPageSize());
        LambdaQueryWrapper<ServiceItem> queryWrapper = new LambdaQueryWrapper<>();

        // 分类精准匹配
        queryWrapper.eq(request.getCategoryId() != null, ServiceItem::getCategoryId, request.getCategoryId());
        // 名称模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(request.getName()), ServiceItem::getName, request.getName());

        // 排序规则：先按权重升序，再按创建时间降序
        queryWrapper.orderByAsc(ServiceItem::getSort).orderByDesc(ServiceItem::getCreateTime);

        return this.page(page, queryWrapper);
    }
}




