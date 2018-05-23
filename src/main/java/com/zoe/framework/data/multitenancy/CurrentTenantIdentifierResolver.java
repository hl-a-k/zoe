package com.zoe.framework.data.multitenancy;

/**
 * current tenant identifier
 *
 * Created by caizhicong on 2017/8/5.
 */
public interface CurrentTenantIdentifierResolver {

    /**
     * Resolve the current tenant identifier.
     *
     * @return The current tenant identifier
     */
    public String resolveCurrentTenantIdentifier();
}