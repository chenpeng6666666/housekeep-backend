package com.itxc.housekeepbackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.address.AddressDto;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    private AddressService addressService;

    @Resource
    private UserService userService;

    /**
     * 获取用户地址列表 (卡片无需分页)
     */
    @GetMapping("/list")
    public BaseResponse<List<Address>> getAddressList(){
        User loginUser = userService.getLoginUser();
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        List<Address> addressList = addressService.list(new QueryWrapper<Address>()
                .eq("user_id", loginUser.getId())
                .orderBy(true, false, "update_time")
        );
        return ResultUtils.success(addressList);
    }

    @PostMapping("/saveOrUpdate")
    public BaseResponse<String> saveOrUpdate(@RequestBody AddressDto addressDto){
        ThrowUtils.throwIf(ObjUtil.isEmpty(addressDto), ErrorCode.PARAMS_ERROR, "地址信息不能为空");
        boolean b = addressService.saveOrUpdateAddress(addressDto);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "保存或新增地址失败");
        return ResultUtils.success("保存或新增地址成功");
    }


    @PostMapping("/setDefault")
    public BaseResponse<String> setDefault(Long addressId){
        ThrowUtils.throwIf(ObjUtil.isNull(addressId), ErrorCode.PARAMS_ERROR, "地址ID不能为空");
        boolean b = addressService.setDefaultAddress(addressId);
        return ResultUtils.success("设置默认地址成功");
    }

    @DeleteMapping("/delete")
    public BaseResponse<String> deleteAddressById(Long addressId){
        // 1 参数检验
        ThrowUtils.throwIf(ObjUtil.isNull(addressId), ErrorCode.PARAMS_ERROR, "地址ID不能为空");
        // 2 查出数据库原有的地址实体
        Address address = addressService.getById(addressId);
        ThrowUtils.throwIf(ObjUtil.isNull(address), ErrorCode.NOT_FOUND_ERROR, "地址不存在");
        // 鉴权
        ThrowUtils.throwIf(!address.getUserId().equals(BaseContext.getCurrentId()), ErrorCode.NO_AUTH_ERROR, "无权限删除");
        boolean b = addressService.removeById(addressId);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "删除地址失败");
        return ResultUtils.success("删除地址成功");
    }


}
