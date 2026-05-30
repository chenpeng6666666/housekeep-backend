package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itxc.housekeepbackend.model.dto.admin.AdminLoginDto;
import com.itxc.housekeepbackend.model.dto.company.CompanyAuditRequest;
import com.itxc.housekeepbackend.model.entity.SysAdmin;
import com.itxc.housekeepbackend.model.vo.AdminLoginVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Lenovo
 * @description 针对表【sys_admin(平台系统管理员表)】的数据库操作Service
 * @createDate 2026-05-25 17:31:13
 */
public interface SysAdminService extends IService<SysAdmin> {

    /**
     * 管理员登录方法
     *
     * @param loginDto 管理登录请求
     * @return 管理员登录信息
     */
    AdminLoginVo login(AdminLoginDto loginDto, HttpServletRequest request);


    /**
     * 获取当前登录用户的 ID
     * @return
     */
    SysAdmin getLoginUser();

    /**
     * 企业审核
     * @param companyAuditRequest
     * @return
     */
    Boolean auditCompany(CompanyAuditRequest companyAuditRequest);

}
