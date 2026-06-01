package com.itxc.housekeepbackend.service;

import com.itxc.housekeepbackend.model.entity.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Lenovo
* @description 针对表【user_address(用户服务地址表)】的数据库操作Service
* @createDate 2026-06-01 19:43:45
*/
public interface UserAddressService extends IService<UserAddress> {


    /**
     * 保存或更新地址
     * @param address 保存或更新地址请求
     * @return 保存或更新结果
     */
    boolean saveOrUpdateAddress(UserAddress address);


}
