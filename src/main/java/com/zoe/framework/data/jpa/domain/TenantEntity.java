package com.zoe.framework.data.jpa.domain;

import javax.persistence.Column;

/**
 * 可租用实体
 * Created by caizhicong on 2017/9/14.
 */
public abstract class TenantEntity implements Tenantable {

    private static final long serialVersionUID = 1L;

    @Column(name = "tenant_id", updatable = false)
    private String tenantId;

    /**
     * 设置租户ID
     * @return
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 获取租户ID
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
