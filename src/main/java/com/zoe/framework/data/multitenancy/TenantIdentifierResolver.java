package com.zoe.framework.data.multitenancy;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created by caizhicong on 2017/8/5.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenant();
        if (tenantId != null) {
            return tenantId;
        }
        return TenantConstants.DEFAULT_TENANT_ID;
    }

    /**
     * 租户ID存放在Http Header
     * @return
     */
    public String resolveCurrentTenantIdentifierInHeader() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr != null) {
            String tenantId = attr.getRequest().getHeader(TenantConstants.TENANT_KEY);
            if (tenantId != null) {
                return tenantId;
            }
        }
        return TenantConstants.DEFAULT_TENANT_ID;
    }
}