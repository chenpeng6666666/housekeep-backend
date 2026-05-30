package com.itxc.housekeepbackend.controller.company;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.CheckCompanyAuditAndAdmin;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.company.CompanyPageRequest;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.vo.CompanyVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Xy
 * @version 1.0
 * @description: 企业请求接口
 * @date 2026/4/4 17:35
 */
@Slf4j
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
    public BaseResponse<Boolean> registerCompany(@RequestBody CompanyRegisterDto dto){
        // 1 参数校验
        ThrowUtils.throwIf(ObjUtil.isEmpty(dto), ErrorCode.PARAMS_ERROR);
        companyService.companyRegister(dto);
        return ResultUtils.success(true);
    }




    /**
     * 企业信息修改(请求参数必须包含id) 企业管理员权限
     */
    @PutMapping("/update")
    public BaseResponse<String> updateCompany(@RequestBody Company company, HttpServletRequest request){
        // 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(company), ErrorCode.PARAMS_ERROR);
        boolean b = companyService.updateCompany(company,request);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR,"企业信息修改失败");
        return ResultUtils.success("企业信息已修改，待审核");

    }

    /**
     * 企业员工信息分页查询 (仅仅企业管理员可用)
     */
//    @GetMapping("/employee/page")
//    @CheckCompanyAuditAndAdmin
//    public BaseResponse<Page<CompanyEmployee>> queryEmployeePage(EmployeeQueryRequest request){
//        // 参数校验
//        ThrowUtils.throwIf(ObjUtil.isNull(request), ErrorCode.PARAMS_ERROR);
//        int current = request.getCurrent();
//        int pageSize = request.getPageSize();
////        Page<CompanyEmployee> page = companyEmployeeService.page(new Page<>(current, pageSize), companyEmployeeService.getQueryWrapper(request));
//
//        return ResultUtils.success(new Page<CompanyEmployee>());
//    }

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


    /**
     * 获取当前员工的所属企业的详细信息
     */
    @GetMapping("/profile")
    public BaseResponse<CompanyVO> getCompanyInfo(HttpServletRequest request){
        CompanyEmployee employee = companyEmployeeService.getLoginEmp(request);
        log.info("当前员工所属企业id：{}", employee.getCompanyId());
        Company company = companyService.getById(employee.getCompanyId());
        // 企业信息脱敏
        CompanyVO companyVO = new CompanyVO();
        BeanUtil.copyProperties(company, companyVO);
        return ResultUtils.success(companyVO);
    }

    /**
     * 企业分页查询
     */
    @GetMapping("/page")
    public BaseResponse<Page<Company>> queryCompanyPage(CompanyPageRequest request){
        // 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(request), ErrorCode.PARAMS_ERROR);
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        Company company = new Company();
        BeanUtil.copyProperties(request, company);
        Page<Company> page = companyService.page(new Page<>(current, pageSize), companyService.getQueryWrapper(company));
        return ResultUtils.success(page);
    }







}
