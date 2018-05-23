package com.zoe.framework.data.jpa.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.util.Date;

/**
 * 实体类基类
 * Created by caizhicong on 2017/7/6.
 */
public abstract class BaseEntity implements AuditableEntity<String, String>, ValidableEntity<String> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private String id;

    @Column(name = "valid_flag", length = 4, nullable = false)
    private Integer validFlag;

    @CreatedBy
    @Column(name = "create_user", nullable = false, updatable = false)
    private String createUser;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", nullable = false, updatable = false)
    private Date createTime;

    @LastModifiedBy
    @Column(name = "modify_user", nullable = false)
    private String modifyUser;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_time", nullable = false)
    private Date modifyTime;

    @Column(name = "remark")
    private String remark;

    /**
     * 获取主键
     */
    public String getId() {
        return this.id;
    }

    /**
     * 设置主键
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取有效标记
     */
    public Integer getValidFlag() {
        return this.validFlag;
    }

    /**
     * 设置有效标记
     */
    public void setValidFlag(Integer validFlag) {
        this.validFlag = validFlag;
    }

    /**
     * 获取创建人
     */
    public String getCreateUser() {
        return this.createUser;
    }

    /**
     * 设置创建人
     */
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    /**
     * 获取创建时间
     */
    public Date getCreateTime() {
        return this.createTime;
    }

    /**
     * 设置创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取修改人
     */
    public String getModifyUser() {
        return this.modifyUser;
    }

    /**
     * 设置修改人
     */
    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    /**
     * 获取修改时间
     */
    public Date getModifyTime() {
        return this.modifyTime;
    }

    /**
     * 设置修改时间
     */
    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }


    /**
     * 获取备注
     */
    public String getRemark() {
        return this.remark;
    }

    /**
     * 设置备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 是否为新创建的实体
     */
    @JsonIgnore
    @JSONField(serialize = false)
    @Override
    public boolean isNew() {
        return id == null || id.isEmpty();
    }
}