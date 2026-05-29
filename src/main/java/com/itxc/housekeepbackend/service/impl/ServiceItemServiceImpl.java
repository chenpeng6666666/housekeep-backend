package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.mapper.ServiceItemMapper;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.service.ServiceItemService;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【service_item(企业服务条目表)】的数据库操作Service实现
* @createDate 2026-04-05 17:52:56
*/
@Service
public class ServiceItemServiceImpl extends ServiceImpl<ServiceItemMapper, ServiceItem>
    implements ServiceItemService {

}




