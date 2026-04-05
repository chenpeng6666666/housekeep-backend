package com.itxc.housekeepbackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.company.EmployeeQueryRequest;
import com.itxc.housekeepbackend.model.dto.companyEmployee.CompanyEmployeeLoginDto;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.vo.CompanyEmployeeLoginVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Xy
 * @version 1.0
 * @description: 企业请求接口
 * @date 2026/4/4 17:35
 */
@RestController
@RequestMapping("/company")
public class CompanyController {

    @Resource
    private CompanyService companyService;

    @Resource
    private CompanyEmployeeService companyEmployeeService;

    /**
     * 企业入驻
     */
    @PostMapping("/register")
    public boolean registerCompany(@RequestBody CompanyRegisterDto dto){
        // 参数校验
        ThrowUtils.throwIf(ObjUtil.isEmpty(dto), ErrorCode.PARAMS_ERROR);
        return companyService.registerCompany(dto);
    }

    /**
     * 企业端登录
     */
    @PostMapping("/login")
    public BaseResponse<CompanyEmployeeLoginVO> companyLogin(@RequestBody CompanyEmployeeLoginDto companyEmployeeLoginDto, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(companyEmployeeLoginDto), ErrorCode.PARAMS_ERROR);
        CompanyEmployeeLoginVO companyLoginVO = companyService.login(companyEmployeeLoginDto,request);
        return ResultUtils.success(companyLoginVO);
    }

    /**
     * 企业员工信息分页查询 (仅仅企业管理员可用)
     */
    @GetMapping("/employee/page")
    public BaseResponse<Page<CompanyEmployee>> queryEmployeePage(EmployeeQueryRequest request){
        // 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(request), ErrorCode.PARAMS_ERROR);
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        Page<CompanyEmployee> page = companyEmployeeService.page(new Page<>(current, pageSize), companyEmployeeService.getQueryWrapper(request));
        return ResultUtils.success(page);
    }

    /**
     * 新增企业员工
     */
    @PostMapping("/employee")
    public BaseResponse<Long> addEmployee(@RequestBody CompanyEmployee companyEmployee){
        ThrowUtils.throwIf(ObjUtil.isNull(companyEmployee), ErrorCode.PARAMS_ERROR);
        return null;
    }













}
