package com.itxc.housekeepbackend.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.exception.BusinessException;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.model.dto.company.EmployeeQueryRequest;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.mapper.CompanyEmployeeMapper;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import org.springframework.stereotype.Service;

/**
 * @author Lenovo
 * @description 针对表【company_employee(企业员工与账号表)】的数据库操作Service实现
 * @createDate 2026-04-04 17:36:14
 */
@Service
public class CompanyEmployeeServiceImpl extends ServiceImpl<CompanyEmployeeMapper, CompanyEmployee>
        implements CompanyEmployeeService {

    @Override
    public QueryWrapper<CompanyEmployee> getQueryWrapper(EmployeeQueryRequest employeeQueryRequest) {
        Long employeeId = BaseContext.getCurrentId();
        CompanyEmployee employee = this.getById(employeeId);
        if (employee.getCompanyId() == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "当前用户未绑定企业");
        }

        QueryWrapper<CompanyEmployee> queryWrapper = new QueryWrapper<>();
        // 获取查询参数
        Long id = employeeQueryRequest.getId();
        String realName = employeeQueryRequest.getRealName();
        String phone = employeeQueryRequest.getPhone();
        Integer workStatus = employeeQueryRequest.getWorkStatus();

        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(realName), "real_name", realName);
        queryWrapper.like(StrUtil.isNotBlank(phone), "phone", phone);
        queryWrapper.eq(ObjUtil.isNotNull(workStatus), "work_status", workStatus);
        // 仅能查询本企业的员工列表
        queryWrapper.eq("company_id", employee.getCompanyId());
        // 排序
        queryWrapper.orderByDesc("creatTime");
        return queryWrapper;
    }
}




