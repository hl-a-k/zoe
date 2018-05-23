package com.zoe.framework.shiro.web.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zoe.framework.shiro.authc.StatelessToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 无状态 AccessControlFilter
 * Created by caizhicong on 2017/4/10.
 */
public class StatelessAccessControlFilter extends AccessControlFilter {

    private Logger logger = LoggerFactory.getLogger(StatelessAccessControlFilter.class);

    private Cookie tokenCookie;

    public StatelessAccessControlFilter() {
        Cookie cookie = new SimpleCookie("token");
        cookie.setHttpOnly(true); //more secure, protects against XSS attacks
        this.tokenCookie = cookie;
    }

    public Cookie getTokenCookie() {
        return tokenCookie;
    }

    /**
     * Returns <code>true</code> if the request is allowed to proceed through the filter normally, or <code>false</code>
     * if the request should be handled by the
     * {@link #onAccessDenied(ServletRequest, ServletResponse, Object) onAccessDenied(request,response,mappedValue)}
     * method instead.
     *
     * @param request     the incoming <code>ServletRequest</code>
     * @param response    the outgoing <code>ServletResponse</code>
     * @param mappedValue the filter-specific config value mapped to this filter in the URL rules mappings.
     * @return <code>true</code> if the request should proceed through the filter normally, <code>false</code> if the
     * request should be processed by this filter's
     * {@link #onAccessDenied(ServletRequest, ServletResponse, Object)} method instead.
     * @throws Exception if an error occurs during processing.
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        return false;
    }

    /**
     * Processes requests where the subject was denied access as determined by the
     * {@link #isAccessAllowed(ServletRequest, ServletResponse, Object) isAccessAllowed}
     * method.
     *
     * @param request  the incoming <code>ServletRequest</code>
     * @param response the outgoing <code>ServletResponse</code>
     * @return <code>true</code> if the request should continue to be processed; false if the subclass will
     * handle/render the response directly.
     * @throws Exception if there is an error processing the request.
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = ((HttpServletRequest) request);
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if(logger.isDebugEnabled()) {
            logger.debug("access url: {}", httpRequest.getRequestURI());
        }
        String tokenValue = httpRequest.getParameter("token");
        if(!StringUtils.hasText(tokenValue)) tokenValue = httpRequest.getHeader("token");
        if(!StringUtils.hasText(tokenValue)) {
            //tokenValue = getTokenCookie().readValue(httpRequest, httpResponse);
        }
        if(!StringUtils.hasText(tokenValue)) {
            String parameterValue = httpRequest.getParameter("parameter");
            if (parameterValue != null) {
                JSONObject parameter = JSON.parseObject(parameterValue);
                tokenValue = parameter.getString("token");
            }
        }

        StatelessToken token = new StatelessToken(tokenValue,tokenValue);
        try {
            //5、委托给Realm进行登录
            Subject subject = getSubject(request, response);
            subject.login(token);
            logger.info("token登录验证成功！（token={}）",tokenValue);
        }
        catch (IncorrectCredentialsException e) {
            logger.error("token登录验证失败！（token={}，url={}） -", tokenValue, httpRequest.getRequestURI(), e);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
            if(isAjaxRequest(httpRequest)) {
                httpResponse.getWriter().write(String.format("{\"code\": 401, \"message\": \"%s\" }", "token登录验证失败！"));
            } else {
                httpResponse.getWriter().write("token登录验证失败！");
            }
            return false;
        }
        catch (Exception e) {
            logger.error("token登录验证失败！（token={}，url={}） -", tokenValue, httpRequest.getRequestURI(), e);
            onLoginFailed(response); //登录失败
            return false;
        }
        return true;
    }

    //登录失败时默认返回401状态码
    private void onLoginFailed(ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("login error");
    }

    /**
     * 判断是否为Ajax请求
     *
     * @param request
     *            HttpServletRequest
     * @return 是true, 否false
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null)
            return false;
        String requestType = request.getHeader("X-Requested-With");
        return (requestType != null) && requestType.equals("XMLHttpRequest");
    }
}
