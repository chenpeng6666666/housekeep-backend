package com.itxc.housekeepbackend.mapper;

import com.itxc.housekeepbackend.model.entity.EmployeeSchedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【employee_schedule(员工工单排班占用表)】的数据库操作Mapper
* @createDate 2026-06-03 19:58:37
* @Entity com.itxc.housekeepbackend.model.entity.EmployeeSchedule
*/
public interface EmployeeScheduleMapper extends BaseMapper<EmployeeSchedule> {


    @Select("<script>" +
            "SELECT DISTINCT employee_id FROM employee_schedule " +
            "WHERE start_time &lt; #{orderEndTime} AND end_time &gt; #{orderStartTime} " +
            "AND employee_id IN " +
            "<foreach collection='candidateIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Long> getBusyEmployeeIds(@Param("orderStartTime") Date orderStartTime,
                                  @Param("orderEndTime") Date orderEndTime,
                                  @Param("candidateIds") List<Long> candidateIds);



}




