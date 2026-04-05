package com.itxc.housekeepbackend.model.dto.company;

import com.itxc.housekeepbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Xy
 * @version 1.0
 * @description: 员工分页查询请求参数
 * @date 2026/4/5 14:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmployeeQueryRequest extends PageRequest {

    /**
     * id
     */
    private Long id;

    // 手机号
    private String phone;

    // 真实姓名
    private String realName;

    // 工作状态
    private Integer workStatus;


}
