package com.itxc.housekeepbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.exception.BusinessException;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.UserAddressMapper;
import com.itxc.housekeepbackend.model.dto.address.AddressDto;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.entity.UserAddress;
import com.itxc.housekeepbackend.service.UserAddressService;
import com.itxc.housekeepbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
* @author Lenovo
* @description 针对表【user_address(用户服务地址表)】的数据库操作Service实现
* @createDate 2026-04-04 14:08:13
*/
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
    implements UserAddressService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // 并发集合
    ConcurrentHashMap<Long, Object> lockMap = new ConcurrentHashMap<>();

    @Override
    public boolean saveOrUpdateAddress(AddressDto addressDto) {
        // 1 判断当前用户是否登录
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        Long id = addressDto.getId();
        // 判断参数不能为空
//        String contactName = addressDto.getContactName();
//        String contactPhone = addressDto.getContactPhone();
//        String province = addressDto.getProvince();
//        String city = addressDto.getCity();
//        String district = addressDto.getDistrict();
//        String detailAddress = addressDto.getDetailAddress();

        UserAddress userAddress = new UserAddress();
        BeanUtil.copyProperties(addressDto, userAddress);
        userAddress.setUpdateTime(new Date());
        userAddress.setUserId(BaseContext.getCurrentId());
        // 2 判断当前方法是新增还是修改
        if (ObjUtil.isNull(id)){ // 新增
            // 2.1 查询当前用户是否存在地址 不存在此地址设为默认地址
            boolean exists = this.exists(new QueryWrapper<UserAddress>()
                    .eq("user_id", userAddress.getUserId()));
            if (!exists){
                addressDto.setIsDefault(1);// 设置为默认地址
            }
            userAddress.setCreateTime(new Date());
        }
        return this.saveOrUpdate(userAddress);
    }

    @Override
    public QueryWrapper<UserAddress> getQueryWrapper(UserAddress userAddress) {
        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        if (userAddress == null){
            return queryWrapper;
        }
        // 查询参数
        Long id = userAddress.getId();
        Long userId = userAddress.getUserId();
        String contactName = userAddress.getContactName();
        String contactPhone = userAddress.getContactPhone();
        String province = userAddress.getProvince();
        String city = userAddress.getCity();
        String district = userAddress.getDistrict();
        String detailAddress = userAddress.getDetailAddress();
        Integer isDefault = userAddress.getIsDefault();

        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "user_id", userId);
        queryWrapper.like(StrUtil.isNotBlank(contactName), "contact_name", contactName);
        queryWrapper.eq(StrUtil.isNotBlank(contactPhone), "contact_phone", contactPhone);
        queryWrapper.eq(StrUtil.isNotBlank(province), "province", province);
        queryWrapper.eq(StrUtil.isNotBlank(city), "city", city);
        queryWrapper.eq(StrUtil.isNotBlank(district), "district", district);
        queryWrapper.like(StrUtil.isNotBlank(detailAddress), "detail_address", detailAddress);
        queryWrapper.eq(ObjUtil.isNotNull(isDefault), "is_default", isDefault);
        // 排序
        queryWrapper.orderBy(true, false, "update_time");
        return queryWrapper;
    }

    @Override
    public boolean setDefaultAddress(Long addressId) {
        // 1 查询用户原本的默认地址
        UserAddress defaultAddress = this.getOne(new QueryWrapper<UserAddress>()
                .eq("user_id", BaseContext.getCurrentId())
                .eq("is_default", 1));
        // 2 查询待修改的地址
        UserAddress updateAddress = this.getOne(new QueryWrapper<UserAddress>()
                .eq("id", addressId));
        // 3 修改用户原本的默认地址
        if (defaultAddress.equals(updateAddress)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"当前地址已为默认地址");
        }
        defaultAddress.setIsDefault(0);
        updateAddress.setIsDefault(1);

        // 加锁
        Long userId = BaseContext.getCurrentId();
        Object lock = lockMap.computeIfAbsent(userId, key -> {
            return new Object();
        });
        synchronized (lock){
            try {
                transactionTemplate.execute(status -> {
                    this.updateById(defaultAddress);
                    this.updateById(updateAddress);
                    return true;
                });
                return true;
            }finally {
                lockMap.remove(userId);
            }
        }
    }
}




