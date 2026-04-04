package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.dto.user.UserRegisterDto;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【user(普通用户表)】的数据库操作Service
* @createDate 2026-04-03 18:12:42
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterDto
     */
    void register(UserRegisterDto userRegisterDto);

    /**
     * 密码加盐 md5 加密
     * @param password
     * @return
     */
    String getEncryptPassword(String password);

    /**
     * 用户登录
     * @param userLoginDto
     * @return
     */
    UserVO login(UserLoginDto userLoginDto, HttpServletRequest request);


    /**
     * 获取脱敏的用户信息
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取当前登录用户的 ID
     * @return
     */
    User getLoginUser();

    /**
     * 构造用户查询条件
     */
    QueryWrapper<User> getQueryWrapper(User user);

}
