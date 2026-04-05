package com.itxc.housekeepbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.exception.BusinessException;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.CompanyMapper;
import com.itxc.housekeepbackend.model.dto.companyEmployee.CompanyEmployeeLoginDto;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.vo.CompanyEmployeeLoginVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.itxc.housekeepbackend.constant.UserConstant.PASSWORD_PRE;

/**
* @author Lenovo
* @description 针对表【company(企业信息表)】的数据库操作Service实现
* @createDate 2026-04-05 12:19:30
*/
@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company>
    implements CompanyService {

    @Resource
    private CompanyEmployeeService companyEmployeeService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public boolean registerCompany(CompanyRegisterDto dto) {
        // 请求参数校验
        ThrowUtils.throwIf(ObjUtil.isEmpty(dto), ErrorCode.PARAMS_ERROR);
        // 1. 校验营业执照是否已被注册
        boolean b = this.exists(new QueryWrapper<Company>().eq("license_no", dto.getLicenseNo()));
        ThrowUtils.throwIf(b, ErrorCode.PARAMS_ERROR, "该营业执照号已被注册");

        // 2. 校验管理员手机号是否已被使用 (注意：现在是去 employee 表查)
        b = companyEmployeeService.exists(
                new QueryWrapper<CompanyEmployee>().eq("phone", dto.getAdminPhone())
        );
        ThrowUtils.throwIf(b, ErrorCode.PARAMS_ERROR, "该手机号已被注册为企业员工/管理员");

        // 3. 核心解法：提前生成双表主键 ID
        long companyId = IdWorker.getId(); // 预生成企业 ID
        long adminId = IdWorker.getId();   // 预生成员工 ID

        // 4. 构建企业信息档案 (CompanyInfo)
        Company companyInfo = new Company();
        BeanUtil.copyProperties(dto, companyInfo);
        companyInfo.setId(companyId);           // 塞入预生成的企业 ID
        companyInfo.setAdminId(adminId);        // 绑定预生成的管理员 ID
        companyInfo.setContactPhone(dto.getAdminPhone());
        companyInfo.setAuditStatus(0);          // 0: 待审核状态
        // 5. 构建企业超级管理员账号 (CompanyEmployee)
        CompanyEmployee adminEmp = new CompanyEmployee();
        adminEmp.setId(adminId);                // 塞入预生成的员工 ID
        adminEmp.setCompanyId(companyId);       // 绑定预生成的企业 ID
        adminEmp.setPhone(dto.getAdminPhone());
        adminEmp.setPassword(getEncryptPassword(dto.getPassword())); // 密码加密
        adminEmp.setRoleType(1);                // 角色 1: 企业管理员
        adminEmp.setWorkStatus(1);              // 状态 1: 空闲

        // 6. 统一执行插入数据库操作 (如果其中一个报错 事务会全部回滚)
        Boolean execute = transactionTemplate.execute(status -> {
            this.save(companyInfo);
            companyEmployeeService.save(adminEmp);
            return true;
        });
        return true;
    }

    @Override
    public CompanyEmployeeLoginVO login(CompanyEmployeeLoginDto companyEmployeeLoginDto, HttpServletRequest request) {
        // 1 参数校验
        String phone = companyEmployeeLoginDto.getPhone();
        String password = companyEmployeeLoginDto.getPassword();
        ThrowUtils.throwIf(phone == null || password == null, ErrorCode.PARAMS_ERROR,"手机号或密码不能为空");
        // 2 查询账号
        CompanyEmployee employee = companyEmployeeService.getOne(new QueryWrapper<CompanyEmployee>()
                .eq("phone", phone));
        ThrowUtils.throwIf(employee == null, ErrorCode.PARAMS_ERROR, "账号不存在");
        Long companyId = employee.getCompanyId();
        ThrowUtils.throwIf(companyId == null, ErrorCode.PARAMS_ERROR, "账号未绑定企业,无法登录");
        // 3 密码比对
        if (!employee.getPassword().equals(getEncryptPassword(password))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }
        // 4 用户信息脱敏 并保存企业信息
        CompanyEmployeeLoginVO companyEmployeeLoginVO = new CompanyEmployeeLoginVO();
        BeanUtil.copyProperties(employee, companyEmployeeLoginVO);
        // 封装企业信息
        Company company = this.getById(employee.getCompanyId());
        companyEmployeeLoginVO.setCompanyName(company.getCompanyName());
        companyEmployeeLoginVO.setAuditStatus(company.getAuditStatus());

        request.getSession().setAttribute("employeeId", employee.getId());
        return null;
    }

    @Override
    public String getEncryptPassword(String password) {
        return DigestUtils.md5DigestAsHex((PASSWORD_PRE + password).getBytes());
    }


}




