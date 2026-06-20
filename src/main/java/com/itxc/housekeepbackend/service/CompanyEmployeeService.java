package com.itxc.housekeepbackend.service;

import com.itxc.housekeepbackend.model.dto.companyEmployee.EmployeeSaveDTO;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itxc.housekeepbackend.model.vo.EmployeeVO;

import jakarta.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【company_employee(企业员工账号表)】的数据库操作Service
* @createDate 2026-05-29 18:38:58
*/
public interface CompanyEmployeeService extends IService<CompanyEmployee> {

    /**
     * 企业员工登录
     * @param userLoginDto
     * @param request
     * @return
     */
    EmployeeVO login(UserLoginDto userLoginDto, HttpServletRequest request);


    /**
     * 获取当前登录员工信息
     * @param request 请求对象
     * @return 当前登录员工的信息
     */
    CompanyEmployee getLoginEmp(HttpServletRequest request);

    /**
     * 获取当前登录员工信息
     * @return 当前登录员工的信息
     */
    CompanyEmployee getLoginEmp();

    /**
     * 新增修改员工/以及技能
     * @param dto
     * @param currentCompanyId
     * @return
     */
    boolean saveOrUpdateEmployeeWithSkills(EmployeeSaveDTO dto, Long currentCompanyId);
}
