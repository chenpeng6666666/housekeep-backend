package com.itxc.housekeepbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.exception.BusinessException;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.SysAdminMapper;
import com.itxc.housekeepbackend.model.dto.admin.AdminLoginDto;
import com.itxc.housekeepbackend.model.entity.SysAdmin;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.vo.AdminLoginVo;
import com.itxc.housekeepbackend.service.SysAdminService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Lenovo
 * @description 针对表【sys_admin(平台系统管理员表)】的数据库操作Service实现
 * @createDate 2026-05-25 17:31:13
 */
@Service
public class SysAdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin>
        implements SysAdminService {


    @Override
    public AdminLoginVo login(AdminLoginDto dto, HttpServletRequest request) {
        // 1. 判空
        ThrowUtils.throwIf(StrUtil.hasBlank(dto.getUsername(), dto.getPassword()), ErrorCode.PARAMS_ERROR, "账号或密码不能为空");

        // 2. 根据用户名查询管理员
        SysAdmin admin = this.getOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, dto.getUsername()));
        ThrowUtils.throwIf(admin == null, ErrorCode.NOT_FOUND_ERROR, "管理员账号不存在");

        // 3. 校验账号状态
        ThrowUtils.throwIf(admin.getStatus() == 0, ErrorCode.FORBIDDEN_ERROR, "该账号已被禁用，请联系超级管理员");

        // 4. 校验密码
//        if (!BCrypt.checkpw(dto.getPassword(), admin.getPassword())) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
//        }
        if (!dto.getPassword().equals(admin.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        // 5. 封装返回结果
        AdminLoginVo vo = new AdminLoginVo();
        BeanUtil.copyProperties(admin, vo);
        // 5.1 强制销毁当前浏览器可能残留的旧 Session (极其重要！防止你先登录了企业端，又登录管理端导致的串号)
        request.getSession().invalidate();

        // 5.2 创建一个崭新的 Session，并将管理员ID存入 Session 中！(注意是 getSession().setAttribute)
        request.getSession(true).setAttribute("admin", admin.getId());
        return vo;
    }

    @Override
    public SysAdmin getLoginUser() {
        Long adminId = BaseContext.getCurrentId();
        SysAdmin admin = this.getById(adminId);
        ThrowUtils.throwIf(admin == null, ErrorCode.NOT_LOGIN_ERROR,"用户未登录");
        return admin;
    }
}




