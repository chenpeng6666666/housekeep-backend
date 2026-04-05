package com.itxc.housekeepbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 用户角色枚举类
 */
@Getter
public enum RoleTypeEnum {

    ADMIN("admin", 1),
    USER("user", 0);


    private final String text;

    private final Integer value;

    RoleTypeEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     * @param value
     * @return
     */
    public static RoleTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        // 遍历匹配
        for (RoleTypeEnum anEnum : RoleTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
