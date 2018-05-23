package com.zoe.framework.shiro.authc;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 无状态token
 * Created by caizhicong on 2017/4/10.
 */
public class StatelessToken implements AuthenticationToken {

    private Object principal;
    private Object credentials;

    public StatelessToken(Object userId, Object credentials) {
        this.principal = userId;
        this.credentials = credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }
}
