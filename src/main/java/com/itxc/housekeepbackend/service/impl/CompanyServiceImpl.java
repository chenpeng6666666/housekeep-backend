package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.constant.EmployeeConstant;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.CompanyMapper;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.vo.CompanyDetailVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.CompanyService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.itxc.housekeepbackend.constant.EmployeeConstant.Employee_ADMIN;
import static com.itxc.housekeepbackend.constant.StatusConstant.AUDIT_STATUS_DRAFT;
import static com.itxc.housekeepbackend.constant.StatusConstant.AUDIT_STATUS_PENDING;

@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

    @Lazy
    @Resource
    private CompanyEmployeeService companyEmployeeService;
    // 编程时事务
    @Resource
    private TransactionTemplate transactionTemplate;


    @Override
    public void companyRegister(CompanyRegisterDto dto) {
        // 1. 校验统一社会信用代码是否已被注册
        long count = this.count(Wrappers.<Company>lambdaQuery().eq(Company::getLicenseNo, dto.getLicenseNo()));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "该统一社会信用代码已被注册");

        // 2. 校验手机号是否已经关联企业
        long phoneCount = companyEmployeeService.count(Wrappers.<CompanyEmployee>lambdaQuery().eq(CompanyEmployee::getPhone, dto.getAdminPhone()));
        ThrowUtils.throwIf(phoneCount > 0, ErrorCode.PARAMS_ERROR, "该手机号已绑定其他企业账号");


        // 3. 创建企业基础信息 (状态默认为 0-待完善信息)
        Company company = new Company();
        company.setCompanyName(dto.getCompanyName());
        company.setLicenseNo(dto.getLicenseNo());
        // 企业首次入驻 将企业状态置为 草稿
        company.setAuditStatus(AUDIT_STATUS_DRAFT);
        transactionTemplate.executeWithoutResult(status -> {
            this.save(company);

            // 4. 创建该企业的默认超级管理员账号
            CompanyEmployee admin = new CompanyEmployee();
            admin.setCompanyId(company.getId());
            admin.setPhone(dto.getAdminPhone());
            admin.setPassword(dto.getPassword()); // TODO 加盐加密 (如 BCrypt)
            admin.setRealName("超级管理员");
            admin.setRoleType(Employee_ADMIN);
            companyEmployeeService.save(admin);
        });
    }

    @Override
    public boolean updateCompany(Company company, HttpServletRequest request) {
        // 判断当前员工是否有权限修改
        CompanyEmployee employee = companyEmployeeService.getLoginEmp(request);
        ThrowUtils.throwIf(!employee.getCompanyId().equals(company.getId()), ErrorCode.NO_AUTH_ERROR, "无权限修改");
        ThrowUtils.throwIf(!employee.getRoleType().equals(EmployeeConstant.Employee_ADMIN), ErrorCode.NO_AUTH_ERROR, "非企业管理员无法修改企业信息");
        // 将审核状态置为待审核
        company.setAuditStatus(AUDIT_STATUS_PENDING);
        company.setRejectReason("");

        return this.updateById(company);
    }

    @Override
    public Wrapper<Company> getQueryWrapper(Company company) {
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(company.getCompanyName() != null, Company::getCompanyName, company.getCompanyName());
        queryWrapper.eq(company.getLicenseNo() != null, Company::getLicenseNo, company.getLicenseNo());
        queryWrapper.like(company.getLegalPerson() != null, Company::getLegalPerson, company.getLegalPerson());
        queryWrapper.eq(company.getCompanyType() != null, Company::getCompanyType, company.getCompanyType());
        queryWrapper.eq(company.getScale() != null, Company::getScale, company.getScale());
        queryWrapper.like(company.getAddress() != null, Company::getAddress, company.getAddress());
        queryWrapper.eq(company.getAuditStatus() != null, Company::getAuditStatus, company.getAuditStatus());
        queryWrapper.orderByDesc(Company::getCreateTime);

        return queryWrapper;
    }

    @Override
    public CompanyDetailVO getCompanyDetailWithEmployees(Long companyId) {
        // 1. 查询企业基本信息
        Company company = this.getById(companyId);
        if (company == null || company.getIsDeleted() == 1) {
            throw new RuntimeException("企业信息不存在或已被移除");
        }

        // 2. 将企业属性拷贝到 VO 对象中
        CompanyDetailVO vo = new CompanyDetailVO();
        BeanUtils.copyProperties(company, vo);

        // 3. 查询该企业旗下的家政员
        LambdaQueryWrapper<CompanyEmployee> empWrapper = new LambdaQueryWrapper<>();
        empWrapper.eq(CompanyEmployee::getCompanyId, companyId)
                .eq(CompanyEmployee::getStatus, 1)     // 只查状态启用的
                .eq(CompanyEmployee::getIsDeleted, 0)  // 未删除的
                .eq(CompanyEmployee::getRoleType, "STAFF") // 排除管理员，只展示服务员工
                .orderByDesc(CompanyEmployee::getCreateTime) // 按入职时间最新排序 (若有评分字段可按评分排)
                .last("LIMIT 3"); // 首页弹窗展示前 3 名即可

        List<CompanyEmployee> topEmployees = companyEmployeeService.list(empWrapper);

        // 4. 数据脱敏：擦除密码字段再返回给前端
        topEmployees.forEach(emp -> emp.setPassword(null));

        // 5. 组合数据
        vo.setTopEmployees(topEmployees);

        return vo;
    }
}