package com.itxc.housekeepbackend.model.vo;

import com.itxc.housekeepbackend.model.entity.Company;
import com.itxc.housekeepbackend.model.entity.CompanyEmployee;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CompanyDetailVO extends Company {
    
    /**
     * 该企业旗下的家政员列表
     */
    private List<CompanyEmployee> topEmployees;

}