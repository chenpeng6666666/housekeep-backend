package com.itxc.housekeepbackend.model.dto.address;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Xy
 * @version 1.0
 * @description: 地址处理请求接口
 * @date 2026/4/4 14:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {

    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址 (如小区、楼栋、门牌号)
     */
    private String detailAddress;

    /**
     * 是否默认地址 (0-否, 1-是)
     */
    private Integer isDefault;

}
