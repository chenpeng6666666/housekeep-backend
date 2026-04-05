package com.itxc.housekeepbackend.annotation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.exception.BusinessException;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.service.CompanyService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Aspect
@Component
public class CompanyAuditAspect {

    @Resource
    private CompanyService companyService;

    // 拦截所有打上了 @CheckCompanyAudit 注解的方法
    @Before("@annotation(com.itxc.housekeepbackend.annotation.CheckCompanyAudit)")
    public void doCheck() {
        // 1. 获取当前登录用户的 ID
        Long currentUserId = BaseContext.getCurrentId();
        
        // 2. 根据管理员 ID 查出对应的企业信息
        Company company = companyService.getOne(
            new QueryWrapper<Company>().eq("admin_id", currentUserId)
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
        
        // 状态为 1 (通过) 则什么都不做，放行请求
        return;
    }
}