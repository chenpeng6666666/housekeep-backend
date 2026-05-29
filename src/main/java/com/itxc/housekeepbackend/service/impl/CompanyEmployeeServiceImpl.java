package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.mapper.CompanyEmployeeMapper;
import org.springframework.stereotype.Service;

/**
* @author Lenovo
* @description 针对表【company_employee(企业员工账号表)】的数据库操作Service实现
* @createDate 2026-05-29 18:38:58
*/
@Service
public class CompanyEmployeeServiceImpl extends ServiceImpl<CompanyEmployeeMapper, CompanyEmployee>
    implements CompanyEmployeeService{

}




