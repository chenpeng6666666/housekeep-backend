package com.itxc.housekeepbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.dto.user.UserRegisterDto;
import com.itxc.housekeepbackend.model.dto.user.UserUpdateDto;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.UserVO;
import com.itxc.housekeepbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.itxc.housekeepbackend.constant.UserConstant.ADMIN;
import static com.itxc.housekeepbackend.constant.UserConstant.USER;

/**
 * @author Xy
 * @version 1.0
 * @description: 用户接口
 * @date 2026/4/3 18:19
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<String> register(@RequestBody UserRegisterDto userRegisterDto){
        String phone = userRegisterDto.getPhone();
        String password = userRegisterDto.getPassword();
        // TODO 验证码
        String code = userRegisterDto.getCode();
        // 1 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(phone), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(password), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.PARAMS_ERROR);

        // 2 注册方法
        userService.register(userRegisterDto);
        return ResultUtils.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<UserVO> login(@RequestBody UserLoginDto userLoginDto, HttpServletRequest request){
        String phone = userLoginDto.getPhone();
        String password = userLoginDto.getPassword();
        // 1 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(phone), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(password), ErrorCode.PARAMS_ERROR);
        // 2 登录方法
        UserVO userVO = userService.login(userLoginDto,request);
        return ResultUtils.success(userVO);
    }

    /**
     * 用户信息修改（普通用户）
     */
    @PutMapping("/update")
    @RequireAuth(USER)
    public BaseResponse<String> update(@RequestBody UserUpdateDto userUpdateDto){
        // 1 参数校验
        ThrowUtils.throwIf(ObjUtil.isEmpty(userUpdateDto), ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        User user = new User();
        BeanUtil.copyProperties(userUpdateDto, user);
        // 2 修改方法
        user.setUpdateTime(new Date());
        boolean b = userService.updateById(user);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "修改失败");
        return ResultUtils.success("修改成功");
    }

    /**
     * 删除用户(仅平台管理员)
     */
    @DeleteMapping
    @RequireAuth(ADMIN)
    public BaseResponse<String> delete(@RequestBody String userId) {
        // 1 请求参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(userId), ErrorCode.PARAMS_ERROR);
        // 2 查询数据库判断用户是否存在
        User user = userService.getById(userId);
        ThrowUtils.throwIf(ObjUtil.isNull(user), ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        // 3 删除用户
        boolean b = userService.removeById(userId);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR, "删除失败");
        return ResultUtils.success("删除成功");
    }

    /**
     * 获取当前登录用户的详细信息
     */
    @GetMapping("/profile")
    @RequireAuth(USER)
    public BaseResponse<User> getProfile() {
        return ResultUtils.success(userService.getLoginUser());
    }





}
