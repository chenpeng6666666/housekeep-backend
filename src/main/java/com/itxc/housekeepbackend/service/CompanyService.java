package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.itxc.housekeepbackend.model.dto.company.CompanyRegisterDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【company(企业信息表)】的数据库操作Service
* @createDate 2026-05-29 18:35:57
*/
public interface CompanyService extends IService<Company> {

    /**
     * 企业入驻
     * @param dto 企业入驻表单
     */
    void companyRegister(CompanyRegisterDto dto);


    /**
     * 企业信息修改
     * @param company 修改的参数
     * @return 是否修改成功
     */
    boolean updateCompany(Company company, HttpServletRequest request);

    /**
     * 获取查询条件
     * @param company
     * @return
     */
    Wrapper<Company> getQueryWrapper(Company company);
}
