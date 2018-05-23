package com.zoe.framework.data.multitenancy;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by caizhicong on 2017/8/5.
 */
//@Component
public class TenantFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String tenant = req.getHeader(TenantConstants.TENANT_KEY);

        if (tenant != null) {
            req.setAttribute(TenantConstants.TENANT_KEY, tenant);
        } else {
            req.setAttribute(TenantConstants.TENANT_KEY, TenantConstants.DEFAULT_TENANT_ID);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }

    /*
    can create tenantFilter bean in configuration
    @Bean
    public FilterRegistrationBean tenantFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Filter tenantFilter = new TenantFilter();
        beanFactory.autowireBean(tenantFilter);
        registration.setFilter(tenantFilter);
        registration.addUrlPatterns("*//*");
        return registration;
    }*/
}