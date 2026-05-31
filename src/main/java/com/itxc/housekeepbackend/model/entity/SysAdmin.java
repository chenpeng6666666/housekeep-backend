package com.itxc.housekeepbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 平台系统管理员表
 * @TableName sys_admin
 */
@TableName(value ="sys_admin")
@Data
public class SysAdmin implements Serializable {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 登录账号(纯字母/数字)
     */
    private String username;

    /**
     * 登录密码(加密存储)
     */
    private String password;

    /**
     * 管理员姓名
     */
    private String realName;

    /**
     * 角色: 0-超级管理员, 1-运营人员
     */
    private Integer roleType;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}