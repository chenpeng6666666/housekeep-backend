package com.itxc.housekeepbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 平台标准服务项目表
 * @TableName service_item
 */
@TableName(value ="service_item")
@Data
public class ServiceItem implements Serializable {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 所属分类ID (关联 service_category)
     */
    private Long categoryId;

    /**
     * 服务项目名称 (如: 深度擦玻璃)
     */
    private String name;

    /**
     * 服务项目封面图
     */
    private String coverImg;

    /**
     * 计费单位 (如: 小时、平米、台、次)
     */
    private String unit;

    /**
     * 平台建议指导价
     */
    private BigDecimal guidancePrice;

    /**
     * 服务标准描述/内容说明
     */
    private String description;

    /**
     * 状态：0-下架禁用, 1-上架正常
     */
    private Integer status;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}