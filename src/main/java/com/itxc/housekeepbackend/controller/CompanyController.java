package com.itxc.housekeepbackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.CheckCompanyAuditAndAdmin;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.company.EmployeeQueryRequest;
import com.itxc.housekeepbackend.model.dto.companyEmployee.CompanyEmployeeLoginDto;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.Company;
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
//        return companyService.registerCompany(dto);
        return true;
    }

    /**
     * 企业端登录
     */
    @PostMapping("/login")
    public BaseResponse<CompanyEmployeeLoginVO> companyLogin(@RequestBody CompanyEmployeeLoginDto companyEmployeeLoginDto, HttpServletRequest request){
        ThrowUtils.throwIf(ObjUtil.isNull(companyEmployeeLoginDto), ErrorCode.PARAMS_ERROR);
//        CompanyEmployeeLoginVO companyLoginVO = companyService.login(companyEmployeeLoginDto,request);
        return ResultUtils.success(null);
    }

    /**
     * 根据用户id查询企业信息
     */
    @GetMapping
    public BaseResponse<Company> getCompanyInfo(){
        Long employeeId = BaseContext.getCurrentId();
        CompanyEmployee employee = companyEmployeeService.getById(employeeId);
        Company company = companyService.getOne(new QueryWrapper<Company>()
                .eq("id", employee.getCompanyId()));
        // TODO 判断员工角色 非管理员用户需要脱敏 暂时不处理
        return ResultUtils.success(company);
    }

    /**
     * 企业信息修改(请求参数必须包含id)
     */
    @PostMapping()
    @CheckCompanyAuditAndAdmin
    public BaseResponse<String> updateCompany(@RequestBody Company company){
        // 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(company), ErrorCode.PARAMS_ERROR);
//        boolean b = companyService.updateCompany(company);
        ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR,"企业信息修改失败");
        return ResultUtils.success("企业信息已修改，待审核");

    }

    /**
     * 企业员工信息分页查询 (仅仅企业管理员可用)
     */
    @GetMapping("/employee/page")
    @CheckCompanyAuditAndAdmin
    public BaseResponse<Page<CompanyEmployee>> queryEmployeePage(EmployeeQueryRequest request){
        // 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(request), ErrorCode.PARAMS_ERROR);
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
//        Page<CompanyEmployee> page = companyEmployeeService.page(new Page<>(current, pageSize), companyEmployeeService.getQueryWrapper(request));

        return ResultUtils.success(new Page<CompanyEmployee>());
    }

    /**
     * 新增企业员工
     */
    @PostMapping("/employee")
    @CheckCompanyAuditAndAdmin
    public BaseResponse<Boolean> addEmployee(@RequestBody CompanyEmployee employee){
        ThrowUtils.throwIf(ObjUtil.isNull(employee), ErrorCode.PARAMS_ERROR);
//        boolean b = companyEmployeeService.saveOrUpdateEmployee(employee);
        return ResultUtils.success(null);
    }

    /**
     * 企业员工信息修改
     */
    @PutMapping("/employee")
    @CheckCompanyAuditAndAdmin
    public BaseResponse<Boolean> updateEmployee(@RequestBody CompanyEmployee employee){
        ThrowUtils.throwIf(ObjUtil.isNull(employee), ErrorCode.PARAMS_ERROR);
//        boolean b = companyEmployeeService.saveOrUpdateEmployee(employee);
        return ResultUtils.success(null);
    }







}
