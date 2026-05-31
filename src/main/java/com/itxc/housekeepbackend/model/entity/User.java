package com.itxc.housekeepbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 普通用户表
 */
@TableName(value ="user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别 (0: 女, 1: 男, 2: 保密)
     */
    private Integer gender;

    /**
     * 用户角色 (0: 普通用户, 1: VIP用户 )
     */
    private Integer roleType;

    /**
     * 状态 (1: 正常, 0: 禁用)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}