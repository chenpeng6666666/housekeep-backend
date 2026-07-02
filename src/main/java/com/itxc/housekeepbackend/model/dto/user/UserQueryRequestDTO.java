package com.itxc.housekeepbackend.model.dto.user;

import com.itxc.housekeepbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequestDTO extends PageRequest {

    /**
     * 手机号（模糊查询）
     */
    private String phone;

    /**
     * 昵称（模糊查询）
     */
    private String nickname;

    /**
     * 状态 (1: 正常, 0: 禁用)
     */
    private Integer status;

}
