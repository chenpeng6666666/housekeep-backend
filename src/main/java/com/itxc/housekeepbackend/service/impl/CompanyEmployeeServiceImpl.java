package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.vo.EmployeeVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.mapper.CompanyEmployeeMapper;
import com.itxc.housekeepbackend.service.CompanyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@Service
public class CompanyEmployeeServiceImpl extends ServiceImpl<CompanyEmployeeMapper, CompanyEmployee>
    implements CompanyEmployeeService{

    @Resource
    private CompanyService companyService;

    @Override
    public EmployeeVO login(UserLoginDto userLoginDto, HttpServletRequest request) {
        String password = userLoginDto.getPassword();
        String phone = userLoginDto.getPhone();
        // 查询当前用户是否存在 且 关联企业存在
        CompanyEmployee employee = this.getOne(new QueryWrapper<CompanyEmployee>()
                .eq("phone", phone)
                .eq("password", password)
        );
        ThrowUtils.throwIf(employee == null, ErrorCode.OPERATION_ERROR,"用户不存在");
        Long companyId = employee.getCompanyId();
        Company company = companyService.getById(companyId);
        ThrowUtils.throwIf(company == null, ErrorCode.OPERATION_ERROR,"所属企业不存在，请联系企业管理员");

        // 封装 VO 对象
        Long employeeId = employee.getId();
        EmployeeVO employeeVO = new EmployeeVO();
        employeeVO.setId(employeeId);
        employeeVO.setRealName(employee.getRealName());
        employeeVO.setRoleType(employee.getRoleType());
        employeeVO.setPhone(employee.getPhone());
        employeeVO.setCompanyId(company.getId());
        employeeVO.setCompanyName(company.getCompanyName());
        employeeVO.setAuditStatus(company.getAuditStatus());
        // session 存储
        request.getSession(true).setAttribute("employeeId", employeeId);
        return employeeVO;
    }

    @Override
    public CompanyEmployee getLoginEmp(HttpServletRequest request) {
        Object id = request.getSession().getAttribute("employeeId");
        CompanyEmployee employee = this.getById((Long) id);
        ThrowUtils.throwIf(employee == null, ErrorCode.OPERATION_ERROR,"用户不存在");
        return employee;
    }
}




