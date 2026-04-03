package com.itxc.housekeepbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.dto.user.UserRegisterDto;
import com.itxc.housekeepbackend.model.vo.UserVO;
import com.itxc.housekeepbackend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Xy
 * @version 1.0
 * @description: 用户接口
 * @date 2026/4/3 18:19
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 用户注册
     * @param userRegisterDto
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<String> register(@RequestBody UserRegisterDto userRegisterDto){
        String phone = userRegisterDto.getPhone();
        String password = userRegisterDto.getPassword();
        String code = userRegisterDto.getCode();
        // 1 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(phone), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(password), ErrorCode.PARAMS_ERROR);
        //ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_ERROR);
        // 2 注册方法
        userService.register(userRegisterDto);
        // 3 TODO 注册完直接登录

        return ResultUtils.success("注册成功");
    }

    @PostMapping("/login")
    public BaseResponse<UserVO> login(@RequestBody UserLoginDto userLoginDto){
        String phone = userLoginDto.getPhone();
        String password = userLoginDto.getPassword();
        // 1 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(phone), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(password), ErrorCode.PARAMS_ERROR);
        // 2 登录方法
        UserVO userVO = userService.login(userLoginDto);
        return ResultUtils.success(userVO);
    }








}
