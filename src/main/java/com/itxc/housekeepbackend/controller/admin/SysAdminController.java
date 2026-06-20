package com.itxc.housekeepbackend.controller.admin;

import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.dto.admin.AdminLoginDto;
import com.itxc.housekeepbackend.model.vo.AdminLoginVo;
import com.itxc.housekeepbackend.service.SysAdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin")
public class SysAdminController {

    @Resource
    private SysAdminService sysAdminService;

    @PostMapping("/login")
    public BaseResponse<AdminLoginVo> login(@RequestBody AdminLoginDto loginDto, HttpServletRequest request) {
        AdminLoginVo vo = sysAdminService.login(loginDto, request);
        return ResultUtils.success(vo);
    }



}