package com.itxc.housekeepbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 企业信息表
 * @TableName company
 */
@TableName(value ="company")
@Data
public class Company implements Serializable {
    /**
     * 企业主键ID
     */
    @TableId
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

    /**
     * 驳回原因
     */
    private String rejectReason;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Company other = (Company) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCompanyName() == null ? other.getCompanyName() == null : this.getCompanyName().equals(other.getCompanyName()))
            && (this.getLicenseNo() == null ? other.getLicenseNo() == null : this.getLicenseNo().equals(other.getLicenseNo()))
            && (this.getLegalPerson() == null ? other.getLegalPerson() == null : this.getLegalPerson().equals(other.getLegalPerson()))
            && (this.getCompanyType() == null ? other.getCompanyType() == null : this.getCompanyType().equals(other.getCompanyType()))
            && (this.getScale() == null ? other.getScale() == null : this.getScale().equals(other.getScale()))
            && (this.getAddress() == null ? other.getAddress() == null : this.getAddress().equals(other.getAddress()))
            && (this.getBusinessLicenseImg() == null ? other.getBusinessLicenseImg() == null : this.getBusinessLicenseImg().equals(other.getBusinessLicenseImg()))
            && (this.getLogo() == null ? other.getLogo() == null : this.getLogo().equals(other.getLogo()))
            && (this.getAuditStatus() == null ? other.getAuditStatus() == null : this.getAuditStatus().equals(other.getAuditStatus()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCompanyName() == null) ? 0 : getCompanyName().hashCode());
        result = prime * result + ((getLicenseNo() == null) ? 0 : getLicenseNo().hashCode());
        result = prime * result + ((getLegalPerson() == null) ? 0 : getLegalPerson().hashCode());
        result = prime * result + ((getCompanyType() == null) ? 0 : getCompanyType().hashCode());
        result = prime * result + ((getScale() == null) ? 0 : getScale().hashCode());
        result = prime * result + ((getAddress() == null) ? 0 : getAddress().hashCode());
        result = prime * result + ((getBusinessLicenseImg() == null) ? 0 : getBusinessLicenseImg().hashCode());
        result = prime * result + ((getLogo() == null) ? 0 : getLogo().hashCode());
        result = prime * result + ((getAuditStatus() == null) ? 0 : getAuditStatus().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDeleted() == null) ? 0 : getIsDeleted().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", companyName=").append(companyName);
        sb.append(", licenseNo=").append(licenseNo);
        sb.append(", legalPerson=").append(legalPerson);
        sb.append(", companyType=").append(companyType);
        sb.append(", scale=").append(scale);
        sb.append(", address=").append(address);
        sb.append(", businessLicenseImg=").append(businessLicenseImg);
        sb.append(", logo=").append(logo);
        sb.append(", auditStatus=").append(auditStatus);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}