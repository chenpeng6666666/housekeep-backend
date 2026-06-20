package com.itxc.housekeepbackend.controller.company;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.companyEmployee.EmployeeSaveDTO;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.entity.EmployeeServiceSkill;
import com.itxc.housekeepbackend.model.vo.EmployeeSkillVO;
import com.itxc.housekeepbackend.model.vo.EmployeeVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.service.EmployeeServiceSkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.itxc.housekeepbackend.constant.EmployeeConstant.Employee_ADMIN;
import static com.itxc.housekeepbackend.constant.UserConstant.COMPANY;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Resource
    private CompanyEmployeeService companyEmployeeService;

    @Resource
    private EmployeeServiceSkillService employeeServiceSkillService;

    /**
     * 企业员工登录
     */
    @PostMapping("/login")
    public BaseResponse<EmployeeVO> employeeLogin(@RequestBody UserLoginDto userLoginDto, HttpServletRequest request) {
        // 1 TODO 参数校验
        EmployeeVO employeeVO = companyEmployeeService.login(userLoginDto, request);
        return ResultUtils.success(employeeVO);
    }


    /**
     * 分页多条件查询员工
     */
    @GetMapping("/page")
    @RequireAuth(COMPANY)
    public BaseResponse<Page<CompanyEmployee>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,// 关键词
            @RequestParam(required = false) String roleType) {
        CompanyEmployee employee = companyEmployeeService.getLoginEmp();
        Page<CompanyEmployee> page = new Page<>(current, pageSize);

        LambdaQueryWrapper<CompanyEmployee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CompanyEmployee::getCompanyId, employee.getCompanyId());
        queryWrapper.eq(roleType != null, CompanyEmployee::getRoleType, roleType);
        queryWrapper.like(keyword != null, CompanyEmployee::getRealName, keyword); // 组合模糊查询：姓名或手机号
        queryWrapper.orderByDesc(CompanyEmployee::getCreateTime);

        Page<CompanyEmployee> result = companyEmployeeService.page(page, queryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 级联保存（新增/编辑）员工及技能评分
     */
    @PostMapping("/save")
    @RequireAuth(COMPANY)
    public BaseResponse<Boolean> save(@RequestBody EmployeeSaveDTO dto) {
        CompanyEmployee employee = companyEmployeeService.getLoginEmp();
        ThrowUtils.throwIf(!employee.getRoleType().equals(Employee_ADMIN), ErrorCode.NO_AUTH_ERROR, "无企业管理员权限");
        boolean success = companyEmployeeService.saveOrUpdateEmployeeWithSkills(dto, employee.getCompanyId());
        return ResultUtils.success(success);
    }

    /**
     * 修改员工账号状态
     */
    @PutMapping("/updateStatus")
    @RequireAuth(COMPANY)
    public BaseResponse<Boolean> updateStatus(@RequestBody CompanyEmployee emp) {
        CompanyEmployee employee = companyEmployeeService.getLoginEmp();
        ThrowUtils.throwIf(!employee.getRoleType().equals(Employee_ADMIN), ErrorCode.NO_AUTH_ERROR, "无企业管理员权限");
        CompanyEmployee target = new CompanyEmployee();
        target.setId(emp.getId());
        target.setStatus(emp.getStatus());
        boolean success = companyEmployeeService.updateById(target);
        return ResultUtils.success(success);
    }

    /**
     * 根据员工ID查询其技能评分 (回显专供)
     */
    @GetMapping("/skills/{employeeId}")
    public BaseResponse<List<EmployeeSkillVO>> getEmployeeSkills(@PathVariable Long employeeId) {
        LambdaQueryWrapper<EmployeeServiceSkill> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmployeeServiceSkill::getEmployeeId, employeeId);

        List<EmployeeServiceSkill> list = employeeServiceSkillService.list(queryWrapper);
        List<EmployeeSkillVO> voList = list.stream().map(item -> {
            EmployeeSkillVO vo = new EmployeeSkillVO();
            vo.setServiceId(item.getServiceId());
            vo.setScore(item.getScore());
            return vo;
        }).collect(Collectors.toList());

        return ResultUtils.success(voList);
    }

    /**
     * 逻辑删除员工
     */
    @DeleteMapping("/delete/{id}")
    @RequireAuth(COMPANY)
    public BaseResponse<Boolean> delete(@PathVariable Long id) {
        CompanyEmployee employee = companyEmployeeService.getLoginEmp();
        ThrowUtils.throwIf(!employee.getRoleType().equals(Employee_ADMIN), ErrorCode.NO_AUTH_ERROR, "无企业管理员权限");
        CompanyEmployee target = new CompanyEmployee();
        target.setId(id);
        target.setIsDeleted(1); // 软删除标记
        boolean success = companyEmployeeService.updateById(target);
        return ResultUtils.success(success);
    }
}
