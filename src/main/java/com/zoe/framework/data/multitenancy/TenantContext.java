package com.zoe.framework.data.multitenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TenantContext
 * Created by caizhicong on 2017/8/5.
 */
public class TenantContext {

    private static Logger logger = LoggerFactory.getLogger(TenantContext.class);

    private static ThreadLocal<String> currentTenant = new ThreadLocal<>();

    private TenantContext() {
    }

    public static String getTenant() {
        return currentTenant.get();
    }

    public static void setTenant(String tenant) {
        logger.debug("Setting tenant to " + tenant);
        currentTenant.set(tenant);
    }

    public static void clear() {
        currentTenant.remove();
    }
}