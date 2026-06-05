package com.itxc.housekeepbackend.mapper;

import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itxc.housekeepbackend.model.vo.CandidateVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【company_employee(企业员工账号表)】的数据库操作Mapper
* @createDate 2026-05-29 18:38:58
* @Entity com.itxc.housekeepbackend.model.entity.CompanyEmployee
*/
public interface CompanyEmployeeMapper extends BaseMapper<CompanyEmployee> {

    // 联合 employee_service_skill 表，查出拥有该技能、评分达标、且在职的普通员工
    @Select("SELECT e.id, e.company_id, s.score " +
            "FROM company_employee e " +
            "INNER JOIN employee_service_skill s ON e.id = s.employee_id " +
            "WHERE s.service_id = #{serviceId} " +
            "AND s.score >= #{requireScore} " +
            "AND e.status = 1 AND e.is_deleted = 0 AND e.role_type = 'STAFF' " +
            "ORDER BY s.score DESC")
    List<CandidateVO> getCandidatesBySkillAndScore(@Param("serviceId") Long serviceId, @Param("requireScore") BigDecimal requireScore);



}




