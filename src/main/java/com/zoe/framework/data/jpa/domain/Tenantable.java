package com.zoe.framework.data.jpa.domain;

import org.springframework.data.domain.Persistable;

import java.io.Serializable;

/**
 * 租户接口
 * Created by caizhicong on 2017/11/6.
 */
public interface Tenantable<ID extends Serializable> extends Persistable<ID> {

    /**
     * 设置租户ID
     * @return
     */
    String getTenantId();

    /**
     * 获取租户ID
     */
    void setTenantId(String tenantId);
}
