package com.zoe.framework.sql2o.datasource;

import com.zoe.framework.data.jpa.repository.support.Sql2oJpaRepository;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DynamicDataSourceRepositoryProxyPostProcessor
 * Created by caizhicong on 2017/9/24.
 */
public class DynamicDataSourceRepositoryProxyPostProcessor implements RepositoryProxyPostProcessor {

    public static final String BEAN_NAME = "dynamicDataSourceRepositoryProxyPostProcessor";

    private final DataSourceInterceptor dataSourceInterceptor = new DataSourceInterceptor();

    /**
     * Manipulates the {@link ProxyFactory}, e.g. add further interceptors to it.
     *
     * @param factory               will never be {@literal null}.
     * @param repositoryInformation will never be {@literal null}.
     */
    @Override
    public void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {
        factory.addAdvice(dataSourceInterceptor);
    }

    /**
     * 数据源切换拦截器
     */
    class DataSourceInterceptor implements MethodInterceptor {

        private final Logger logger = LoggerFactory.getLogger(DataSourceInterceptor.class);
        private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

        private Class getRealClass(MethodInvocation methodInvocation) {
            Class clazz = null;
            Object proxy = ((ReflectiveMethodInvocation) methodInvocation).getProxy();
            if (AopUtils.isJdkDynamicProxy(proxy)) {
                InvocationHandler handler = (InvocationHandler) new DirectFieldAccessor(proxy).getPropertyValue("h");
                AdvisedSupport advised = (AdvisedSupport) new DirectFieldAccessor(handler).getPropertyValue("advised");
                if (advised.getProxiedInterfaces().length > 0) {
                    clazz = advised.getProxiedInterfaces()[0];
                }
            } else {
                clazz = AopUtils.getTargetClass(proxy);
            }
            return clazz;
        }

        private String getMethodKey(MethodInvocation methodInvocation) {
            String cacheKey;
            if (methodInvocation.getThis().getClass() == Sql2oJpaRepository.class) {
                //注解直接加在Repository上的，整各类使用同一个数据源
                cacheKey = getRealClass(methodInvocation).getCanonicalName();
            } else {
                cacheKey = methodInvocation.getThis().getClass().getCanonicalName() + "." + methodInvocation.getMethod().getName();
            }
            return cacheKey;
        }

        private DataSource detectDataSource(MethodInvocation methodInvocation) {
            String cacheKey = getMethodKey(methodInvocation);
            DataSource dataSource = dataSources.get(cacheKey);
            if (dataSource != null) return dataSource;

            dataSource = methodInvocation.getMethod().getAnnotation(DataSource.class);
            if (dataSource == null) {
                dataSource = methodInvocation.getMethod().getDeclaringClass().getAnnotation(DataSource.class);
            }
            if (dataSource == null) {
                dataSource = methodInvocation.getThis().getClass().getAnnotation(DataSource.class);
            }
            if (dataSource == null && methodInvocation instanceof ReflectiveMethodInvocation) {
                try {
                    Class clazz = getRealClass(methodInvocation);
                    if (clazz != null) {
                        cacheKey = clazz.getCanonicalName();
                        dataSource = (DataSource) clazz.getAnnotation(DataSource.class);
                    }
                } catch (Exception ex) {
                    logger.error("获取代理接口发生异常" + ex.getMessage(), ex);
                }
            }

            if (dataSource != null) {
                dataSources.put(cacheKey, dataSource);
            }
            return dataSource;
        }

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            //detect @DataSource
            String cacheKey = getMethodKey(methodInvocation);
            String dsOld = DynamicDataSourceContextHolder.get();
            DataSource dataSource = detectDataSource(methodInvocation);
            String dsName = dataSource != null ? dataSource.value() : null;
            boolean canRestore = !Objects.equals(dsOld, dsName);
            if (dsName != null) {
                if (!DynamicDataSourceContextHolder.exists(dsName)) {
                    logger.error("数据源[{}]不存在，使用默认数据源", dsName);
                } else {
                    logger.debug("{} Use DataSource : {}", cacheKey, dsName);
                    DynamicDataSourceContextHolder.set(dsName);
                }
            }
            Object proceed = methodInvocation.proceed();
            if (dsName != null && canRestore) {
                logger.debug("{} Restore DataSource : {}", cacheKey, dsName);
                DynamicDataSourceContextHolder.remove();
            }
            return proceed;
        }
    }
}
