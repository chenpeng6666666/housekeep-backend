package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.mapper.CompanyMapper;
import com.itxc.housekeepbackend.mapper.EmployeeServiceSkillMapper;
import com.itxc.housekeepbackend.model.dto.companyEmployee.EmployeeSaveDTO;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.itxc.housekeepbackend.model.entity.EmployeeServiceSkill;
import com.itxc.housekeepbackend.model.vo.EmployeeVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import com.itxc.housekeepbackend.mapper.CompanyEmployeeMapper;
import com.itxc.housekeepbackend.service.CompanyService;
import com.itxc.housekeepbackend.service.EmployeeServiceSkillService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CompanyEmployeeServiceImpl extends ServiceImpl<CompanyEmployeeMapper, CompanyEmployee>
    implements CompanyEmployeeService{

    @Resource
    private CompanyMapper companyMapper;

    @Resource
    private EmployeeServiceSkillMapper employeeServiceSkillMapper;

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
        Company company = companyMapper.selectById(companyId);
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

    @Override
    public CompanyEmployee getLoginEmp() {
        Long currentId = BaseContext.getCurrentId();
        CompanyEmployee employee = this.getById(currentId);
        ThrowUtils.throwIf(employee == null, ErrorCode.OPERATION_ERROR,"用户不存在");
        return employee;
    }


    @Override
    @Transactional(rollbackFor = Exception.class) // 任何异常均回滚，确保数据一致性
    public boolean saveOrUpdateEmployeeWithSkills(EmployeeSaveDTO dto, Long currentCompanyId) {
        CompanyEmployee employee = new CompanyEmployee();
        BeanUtils.copyProperties(dto, employee);
        employee.setCompanyId(currentCompanyId); // 强绑定当前登录的企业账户上下文

        boolean isUpdate = dto.getId() != null;

        if (isUpdate) {
            // 更新操作：处理密码置空不修改的逻辑
            if (StringUtils.isBlank(dto.getPassword())) {
                employee.setPassword(null);
            } else {
                // TODO employee.setPassword(bCryptPasswordEncoder.encode(dto.getPassword())); // 如果有加密，在此处处理
            }
            this.updateById(employee);
        } else {
            // 新增操作
            // TODO employee.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
            employee.setStatus(1); // 默认启用
            this.save(employee);
        }

        // --- 开始级联处理关联的技能数据 ---
        Long employeeId = employee.getId();

        if (isUpdate) {
            // 1. 如果是更新操作，先物理删除该员工历史绑定的全部技能
            LambdaQueryWrapper<EmployeeServiceSkill> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(EmployeeServiceSkill::getEmployeeId, employeeId);
            employeeServiceSkillMapper.delete(deleteWrapper);
        }

        // 2. 如果角色是普通员工 (STAFF) 且前端传了技能列表，则执行批量插入
        if ("STAFF".equals(dto.getRoleType()) && dto.getSkills() != null && !dto.getSkills().isEmpty()) {
            List<EmployeeServiceSkill> skillEntities = dto.getSkills().stream().map(skillDto -> {
                EmployeeServiceSkill skill = new EmployeeServiceSkill();
                skill.setEmployeeId(employeeId);
                skill.setServiceId(skillDto.getServiceId());
                skill.setScore(skillDto.getScore());
                return skill;
            }).collect(Collectors.toList());

            // 批量保存技能（因数量一般不大，可直接循环插入或利用底层批量方法）
            for (EmployeeServiceSkill skill : skillEntities) {
                employeeServiceSkillMapper.insert(skill);
            }
        }

        return true;
    }
}




