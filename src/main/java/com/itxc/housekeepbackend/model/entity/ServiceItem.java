package com.itxc.housekeepbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 企业服务条目表
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
     * 归属企业ID
     */
    private Long companyId;

    /**
     * 关联分类ID
     */
    private Long categoryId;

    /**
     * 服务名称
     */
    private String itemName;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 单位
     */
    private String unit;

    /**
     * 预计耗时(分钟)
     */
    private Integer duration;

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
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}