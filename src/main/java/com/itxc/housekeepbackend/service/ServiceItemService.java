package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.model.dto.serviceItem.ServiceItemQueryRequest;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Lenovo
* @description 针对表【service_item(平台标准服务项目表)】的数据库操作Service
* @createDate 2026-05-31 12:37:35
*/
public interface ServiceItemService extends IService<ServiceItem> {

    /**
     * 分页查询
     * @param request
     * @return
     */
    Page<ServiceItem> pageQuery(ServiceItemQueryRequest request);

}
