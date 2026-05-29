package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.CompanyMapper;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements CompanyService {

    @Resource
    private CompanyEmployeeService companyEmployeeService;
    // 编程时事务
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 企业首次入驻申请 (核心事务逻辑)
     */
    @Override
    public void companyRegister(CompanyRegisterDto dto) {
        // 1. 校验统一社会信用代码是否已被注册
        long count = this.count(Wrappers.<Company>lambdaQuery().eq(Company::getLicenseNo, dto.getLicenseNo()));
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "该统一社会信用代码已被注册");

        // 2. 校验手机号是否已被绑定
        long phoneCount = companyEmployeeService.count(Wrappers.<CompanyEmployee>lambdaQuery().eq(CompanyEmployee::getPhone, dto.getAdminPhone()));
        ThrowUtils.throwIf(phoneCount > 0, ErrorCode.PARAMS_ERROR, "该手机号已绑定其他企业账号");


        // 3. 创建企业基础信息 (状态默认为 0-待完善信息)
        Company company = new Company();
        company.setCompanyName(dto.getCompanyName());
        company.setLicenseNo(dto.getLicenseNo());
        company.setAuditStatus(0);
        transactionTemplate.executeWithoutResult(status -> {
            this.save(company);

            // 4. 创建该企业的默认超级管理员账号
            CompanyEmployee admin = new CompanyEmployee();
            admin.setCompanyId(company.getId());
            admin.setPhone(dto.getAdminPhone());
            admin.setPassword(dto.getPassword()); // TODO 加盐加密 (如 BCrypt)
            admin.setRealName("超级管理员");
            admin.setRoleType("ADMIN");
            companyEmployeeService.save(admin);
        });



    }
}