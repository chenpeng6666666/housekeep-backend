package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.mapper.ServiceCategoryMapper;
import com.itxc.housekeepbackend.model.entity.ServiceCategory;
import com.itxc.housekeepbackend.service.ServiceCategoryService;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【service_category(服务分类表)】的数据库操作Service实现
* @createDate 2026-05-25 19:15:05
*/
@Service
public class ServiceCategoryServiceImpl extends ServiceImpl<ServiceCategoryMapper, ServiceCategory>
    implements ServiceCategoryService {

}




