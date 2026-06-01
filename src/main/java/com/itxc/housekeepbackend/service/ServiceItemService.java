package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.model.dto.serviceItem.ServiceItemQueryRequest;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ServiceItemService extends IService<ServiceItem> {

    /**
     * 分页查询
     * @param request 分页请求参数
     * @return 分页结果
     */
    Page<ServiceItem> pageQuery(ServiceItemQueryRequest request);

}
