package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itxc.housekeepbackend.model.dto.companyEmployee.CompanyEmployeeLoginDto;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.vo.CompanyEmployeeLoginVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【company(企业信息表)】的数据库操作Service
* @createDate 2026-04-05 12:19:30
*/
public interface CompanyService extends IService<Company> {

    /**
     * 注册企业
     * @param dto
     * @return
     */
    boolean registerCompany(CompanyRegisterDto dto);

    /**
     * 企业登录
     */
    CompanyEmployeeLoginVO login(CompanyEmployeeLoginDto companyEmployeeLoginDto, HttpServletRequest request);

    /**
     * 密码加盐 md5 加密
     * @param password
     * @return
     */
    String getEncryptPassword(String password);


}
