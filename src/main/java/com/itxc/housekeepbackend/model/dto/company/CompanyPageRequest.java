package com.itxc.housekeepbackend.model.dto.company;

import com.itxc.housekeepbackend.common.PageRequest;
import lombok.Data;

/**
 * @author Xy
 * @version 1.0
 * @description: 企业分页查询请求参数
 * @date 2026/5/30 19:00
 */
@Data
public class CompanyPageRequest extends PageRequest {


    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 企业审核状态
     */
    private Integer auditStatus;


}
