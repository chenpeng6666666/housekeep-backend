package com.itxc.housekeepbackend.model.dto.companyEmployee;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class EmployeeSaveDTO {
    private Long id;              // 修改时必填，新增时为 null
    private String phone;         // 登录手机号
    private String password;      // 登录密码 (新增必填，修改时留空代表不修改)
    private String realName;      // 员工姓名
    private String roleType;      // 角色：ADMIN 或 STAFF
    private Integer status;       // 账号状态：0-禁用, 1-启用

    /** 绑定的服务技能列表 */
    private List<SkillScoreDTO> skills;

    @Data
    public static class SkillScoreDTO {
        private Long serviceId;   // 服务项目ID
        private BigDecimal score; // 能力评分 (1.0 - 5.0)
    }
}