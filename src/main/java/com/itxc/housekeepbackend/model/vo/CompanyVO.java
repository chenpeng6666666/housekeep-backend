package com.itxc.housekeepbackend.model.vo;

import lombok.Data;

/**
 * @author Xy
 * @version 1.0
 * @description: TODO 企业脱敏信息
 * @date 2026/4/5 15:40
 */
@Data
public class CompanyVO {

    /**
     * 企业ID
     */
    private Long id;

    /**
     * 企业完整名称
     */
    private String companyName;

    /**
     * 统一社会信用代码
     */
    private String licenseNo;

    /**
     * 法定代表人
     */
    private String legalPerson;

    /**
     * 企业类型 (如: 有限责任公司、个体工商户)
     */
    private String companyType;

    /**
     * 企业规模 (如: 0-20人, 20-99人, 100人以上)
     */
    private String scale;

    /**
     * 企业详细地址
     */
    private String address;

    /**
     * 营业执照OSS图片地址
     */
    private String businessLicenseImg;

    /**
     * 企业Logo
     */
    private String logo;

    /**
     * 审核状态：0-待完善信息(草稿), 1-待平台审核, 2-审核通过, 3-审核驳回
     */
    private Integer auditStatus;


}
