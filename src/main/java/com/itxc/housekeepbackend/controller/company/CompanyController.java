package com.itxc.housekeepbackend.controller.company;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.CheckCompanyAuditAndAdmin;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.company.CompanyAuditRequest;
import com.itxc.housekeepbackend.model.dto.company.CompanyPageRequest;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.vo.CompanyDetailVO;
import com.itxc.housekeepbackend.model.vo.CompanyVO;
import com.itxc.housekeepbackend.model.vo.OrderVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.CompanyService;
import com.itxc.housekeepbackend.service.SysAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.itxc.housekeepbackend.constant.StatusConstant.AUDIT_STATUS_SUCCESS;
import static com.itxc.housekeepbackend.constant.UserConstant.ADMIN;
import static com.itxc.housekeepbackend.constant.UserConstant.COMPANY;

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

    @Resource
    private SysAdminService sysAdminService;

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
    @RequireAuth(COMPANY)
    public BaseResponse<String> updateCompany(@RequestBody Company company, HttpServletRequest request){
        // 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(company), ErrorCode.PARAMS_ERROR);
        boolean b = companyService.updateCompany(company,request);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR,"企业信息修改失败");
        return ResultUtils.success("企业信息已修改，待审核");
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

    /**
     * 管理员审核企业
     */
    @PutMapping("/audit")
    @RequireAuth(ADMIN)
    public BaseResponse<Boolean> auditCompany(@RequestBody CompanyAuditRequest companyAuditRequest){
        // 1 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(companyAuditRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(sysAdminService.auditCompany(companyAuditRequest));
    }

    /**
     * 前端首页展示平台签约企业
     */
    @GetMapping("/recommend")
    public BaseResponse<List<Company>> recommend() {
        // 后端需要过滤平台已经审核通过的规模最大的三家企业
        List<Company> companyList = companyService.lambdaQuery()
                .eq(Company::getAuditStatus, AUDIT_STATUS_SUCCESS)
                .orderByDesc(Company::getScale).last("limit 3")
                .list();
        return ResultUtils.success(companyList);
    }

    /**
     *  获取企业信息及旗下员工
     */
    @GetMapping("/info/{id}")
    public BaseResponse<CompanyDetailVO> getCompanyDetail(@PathVariable Long id) {
        // 校验参数

        // 调用 Service 获取聚合数据
        CompanyDetailVO companyDetailVO = companyService.getCompanyDetailWithEmployees(id);
        return ResultUtils.success(companyDetailVO);
    }





}
