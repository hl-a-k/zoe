package com.zoe.framework.shiro.realm;

import com.zoe.framework.shiro.authc.StatelessToken;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 无状态 AuthorizingRealm
 * Created by caizhicong on 2017/4/10.
 */
public class StatelessAuthorizingRealm extends AuthorizingRealm {

    private Logger logger = LoggerFactory.getLogger(StatelessAuthorizingRealm.class);

    @Override
    public boolean supports(AuthenticationToken token) {
        //仅支持StatelessToken类型的Token
        return token instanceof StatelessToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String userId = (String) principalCollection.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        Set<String> roles = new HashSet<>();//todo 根据userId获取roles
        Set<String> stringPermissions = new HashSet<>();//todo 根据userId获取stringPermissions
        authorizationInfo.setRoles(roles);
        authorizationInfo.setStringPermissions(stringPermissions);
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        StatelessToken statelessToken = (StatelessToken) authenticationToken;
        String userId = (String) statelessToken.getPrincipal();
        //todo sso获取用户信息

        String username = userId;
        String password = userId;

        logger.info("SSO获取用户信息");
        return new SimpleAuthenticationInfo(username, password, getName());
    }
}
