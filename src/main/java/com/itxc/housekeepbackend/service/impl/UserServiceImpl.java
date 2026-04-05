package com.itxc.housekeepbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.UserMapper;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.dto.user.UserRegisterDto;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.UserVO;
import com.itxc.housekeepbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static com.itxc.housekeepbackend.constant.UserConstant.PASSWORD_PRE;

/**
* @author Lenovo
* @description 针对表【user(普通用户表)】的数据库操作Service实现
* @createDate 2026-04-03 18:12:42
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Override
    public void register(UserRegisterDto userRegisterDto) {
        String phone = userRegisterDto.getPhone();
        String password = userRegisterDto.getPassword();
        String code = userRegisterDto.getCode();
        // 1 查询当前是否已经注册
        boolean exists = this.exists(new QueryWrapper<User>()
                .eq("phone", phone));
        ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR,"当前手机账号已经注册");
        // 2 TODO 校验验证码是否一致 使用 redis 做验证码校验工具

        // 3 MD5密码加密 加盐处理
        User user = new User();
        user.setNickname("用户_" + phone.substring(7)); // 默认昵称
        user.setPhone(phone);
        user.setPassword(getEncryptPassword(password));
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        // 4 新增用户
        this.save(user);
    }

    @Override
    public String getEncryptPassword(String password) {
        return DigestUtils.md5DigestAsHex((PASSWORD_PRE + password).getBytes());
    }

    @Override
    public UserVO login(UserLoginDto userLoginDto, HttpServletRequest request) {
        String phone = userLoginDto.getPhone();
        String password = userLoginDto.getPassword();
        // 1 查数据库账号判断密码是否一致
        User user = this.getOne(new QueryWrapper<User>()
                .eq("phone", phone));
        ThrowUtils.throwIf(user == null, ErrorCode.OPERATION_ERROR,"用户不存在");
        // 密码比对
        boolean equals = user.getPassword().equals(getEncryptPassword(password));
        ThrowUtils.throwIf(!equals, ErrorCode.OPERATION_ERROR,"密码错误");
        // 2 用户信息脱敏
        UserVO userVO = getUserVO(user);
        request.getSession().setAttribute("userId", user.getId());
        return userVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public User getLoginUser() {
        Long userId = BaseContext.getCurrentId();
        User user = this.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        return user;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (user == null) {
            return queryWrapper;
        }
        // 查询参数
        Long id = user.getId();
        String phone = user.getPhone();
        String nickname = user.getNickname();
        String password = user.getPassword();
        Integer status = user.getStatus();
        Date createTime = user.getCreateTime();

        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotNull(phone), "phone", phone);
        queryWrapper.like(ObjUtil.isNotNull(nickname), "nickname", nickname);
        queryWrapper.eq(ObjUtil.isNotNull(password), "password", password);
        queryWrapper.eq(ObjUtil.isNotNull(status), "status", status);
        // 排序
        queryWrapper.orderBy(true, false, "create_time");
        return queryWrapper;
    }


}




