package com.itxc.housekeepbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itxc.housekeepbackend.model.dto.company.EmployeeQueryRequest;
import com.itxc.housekeepbackend.model.entity.Employee;

/**
* @author Lenovo
* @description 针对表【company_employee(企业员工与账号表)】的数据库操作Service
* @createDate 2026-04-04 17:36:14
*/
public interface EmployeeService extends IService<Employee> {

    /**
     * 获取员工查询条件
     * @param employeeQueryRequest
     * @return
     */
    QueryWrapper<Employee> getQueryWrapper(EmployeeQueryRequest employeeQueryRequest);

    /**
     * 新增或修改员工
     * @param employee
     * @return
     */
    boolean saveOrUpdateEmployee(Employee employee);

}
