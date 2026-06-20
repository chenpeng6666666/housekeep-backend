package com.itxc.housekeepbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.entity.UserAddress;
import com.itxc.housekeepbackend.service.UserAddressService;
import com.itxc.housekeepbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * @author Xy
 * @version 1.0
 * @description: 用户地址请求接口
 * @date 2026/4/4 14:06
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Resource
    private UserAddressService userAddressService;

    @Resource
    private UserService userService;

    /**
     * 获取当前用户的地址列表
     */
    @GetMapping("/list")
    @RequireAuth
    public BaseResponse<List<UserAddress>> list() {
        User loginUser = userService.getLoginUser();
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getUserId, loginUser.getId())
                .orderByDesc(UserAddress::getIsDefault) // 默认地址排在最前面
                .orderByDesc(UserAddress::getCreateTime);
        return ResultUtils.success(userAddressService.list(wrapper));
    }

    /**
     * 新增或修改地址
     */
    @PostMapping("/save")
    @RequireAuth
    public BaseResponse<Boolean> saveAddress(@RequestBody UserAddress address) {
        User loginUser = userService.getLoginUser();
        address.setUserId(loginUser.getId());

        boolean result = userAddressService.saveOrUpdateAddress(address);
        return ResultUtils.success(result);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete/{id}")
    @RequireAuth
    public BaseResponse<Boolean> deleteAddress(@PathVariable Long id) {
        User loginUser = userService.getLoginUser();
        // 为了安全，删除时可以加上 userId 校验，防止越权删除别人的地址
        LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAddress::getId, id).eq(UserAddress::getUserId, loginUser.getId());
        boolean result = userAddressService.remove(wrapper);
        return ResultUtils.success(result);
    }



}
