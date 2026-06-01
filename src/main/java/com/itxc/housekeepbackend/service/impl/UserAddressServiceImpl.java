package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.model.entity.UserAddress;
import com.itxc.housekeepbackend.service.UserAddressService;
import com.itxc.housekeepbackend.mapper.UserAddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Lenovo
* @description 针对表【user_address(用户服务地址表)】的数据库操作Service实现
* @createDate 2026-06-01 19:43:45
*/
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
    implements UserAddressService{

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateAddress(UserAddress address) {
        // 核心逻辑：如果当前保存的地址是“默认地址”，则先把该用户下的其他所有地址设为“非默认”
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, address.getUserId())
                    .set(UserAddress::getIsDefault, 0);
            this.update(updateWrapper);
        }
        return this.saveOrUpdate(address);
    }

}




