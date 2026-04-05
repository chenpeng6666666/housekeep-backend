package com.itxc.housekeepbackend.annotation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.exception.BusinessException;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.Employee;
import com.itxc.housekeepbackend.service.CompanyService;
import com.itxc.housekeepbackend.service.EmployeeService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Aspect
@Component
public class CompanyAuditAspect {

    @Resource
    private CompanyService companyService;

    @Resource
    private EmployeeService employeeService;

    // 只有企业审核状态正常且管理员用户才能访问
    @Before("@annotation(com.itxc.housekeepbackend.annotation.CheckCompanyAuditAndAdmin)")
    public void doCheck() {
        // 1. 获取当前登录用户的 ID
        Long employeeId = BaseContext.getCurrentId();
        Employee employee = employeeService.getById(employeeId);

        // 2. 判断当前用户关联的企业是否存在
        Company company = companyService.getOne(
            new QueryWrapper<Company>().eq("id", employee.getCompanyId())
        );
        if (company == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到关联的企业信息");
        }

        // 3. 核心拦截逻辑：判断审核状态
        if (company.getAuditStatus() == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "企业信息正在审核中，暂无权限进行此操作");
        }
        if (company.getAuditStatus() == 2) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "企业入驻被驳回，请修改资料后重新提交审核");
        }

        // 4. 判断当前员工是否为管理员
        if (employee.getRoleType() != 1) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "当前用户不是管理员，无权限进行此操作");
        }
        
        // 状态为 1 (通过) 则什么都不做，放行请求
        return;
    }
}