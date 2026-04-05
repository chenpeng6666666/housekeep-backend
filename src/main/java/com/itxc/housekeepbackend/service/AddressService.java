package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itxc.housekeepbackend.model.dto.address.AddressDto;
import com.itxc.housekeepbackend.model.entity.Address;

/**
* @author Lenovo
* @description 针对表【user_address(用户服务地址表)】的数据库操作Service
* @createDate 2026-04-04 14:08:13
*/
public interface AddressService extends IService<Address> {

    /**
     * 新增或修改用户服务地址
     * @param addressDto
     * @return
     */
    boolean saveOrUpdateAddress(AddressDto addressDto);


    /**
     * 构造用户地址查询条件
     * @param userAddress
     * @return
     */
    QueryWrapper<Address> getQueryWrapper(Address userAddress);

    /**
     * 设置默认地址
     * @param addressId
     * @return
     */
    boolean setDefaultAddress(Long addressId);
}
