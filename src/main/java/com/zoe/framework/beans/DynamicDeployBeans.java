package com.zoe.framework.beans;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>User: Zhang Kaitao
 * <p>Date: 14-1-3
 * <p>Version: 1.0
 */
public class DynamicDeployBeans {

    protected static final Logger logger = LoggerFactory.getLogger(DynamicDeployBeans.class);

    //RequestMappingHandlerMapping
    private static Method detectHandlerMethodsMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "detectHandlerMethods", Object.class);
    private static Method getMappingForMethodMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);
    private static Method getMappingPathPatternsMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingPathPatterns", RequestMappingInfo.class);
    private static Method getPathMatcherMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getPathMatcher");
    private static Method getHandlerMethodsMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getHandlerMethods");
    private static Method unregisterMappingMethod =
            ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "unregisterMapping", Object.class);

    private static Field injectionMetadataCacheField =
            ReflectionUtils.findField(AutowiredAnnotationBeanPostProcessor.class, "injectionMetadataCache");

    static {
        detectHandlerMethodsMethod.setAccessible(true);
        getMappingForMethodMethod.setAccessible(true);
        getMappingPathPatternsMethod.setAccessible(true);
        getPathMatcherMethod.setAccessible(true);
        getHandlerMethodsMethod.setAccessible(true);
        unregisterMappingMethod.setAccessible(true);

        injectionMetadataCacheField.setAccessible(true);
    }

    private ApplicationContext ctx;
    private DefaultListableBeanFactory beanFactory;
    private Map<String, Long> scriptLastModifiedMap = new ConcurrentHashMap<>();//in millis

    public DynamicDeployBeans() {
        this(-1L);
    }

    public DynamicDeployBeans(Long scriptCheckInterval) {
        if (scriptCheckInterval > 0L) {
            startScriptModifiedCheckThead(scriptCheckInterval);
        }
    }

    public DefaultListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    @Autowired
    public void setApplicationContext(ApplicationContext ctx) {
        if (!DefaultListableBeanFactory.class.isAssignableFrom(ctx.getAutowireCapableBeanFactory().getClass())) {
            throw new IllegalArgumentException("BeanFactory must be DefaultListableBeanFactory type");
        }
        this.ctx = ctx;
        this.beanFactory = (DefaultListableBeanFactory) ctx.getAutowireCapableBeanFactory();
    }

    public void registerBean(Class<?> beanClass) {
        registerBean(null, beanClass);
    }

    public void registerBean(String beanName, Class<?> beanClass) {
        Assert.notNull(beanClass, "register bean class must not null");
        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(beanClass);

        if (StringUtils.hasText(beanName)) {
            beanFactory.registerBeanDefinition(beanName, bd);
        } else {
            BeanDefinitionReaderUtils.registerWithGeneratedName(bd, beanFactory);
        }
    }

    public void registerController(Class<?> controllerClass) {
        Assert.notNull(controllerClass, "register controller bean class must not null");
        if (!WebApplicationContext.class.isAssignableFrom(ctx.getClass())) {
            throw new IllegalArgumentException("applicationContext must be WebApplicationContext type");
        }

        GenericBeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClass(controllerClass);

        String controllerBeanName = controllerClass.getName();
        removeOldControllerMapping(controllerBeanName);
        beanFactory.registerBeanDefinition(controllerBeanName, bd);
        addControllerMapping(controllerBeanName);
    }

    public void registerBean(Class<?> beanClass,boolean isController) {
        Assert.notNull(beanClass, "register bean class must not null");
        if (!WebApplicationContext.class.isAssignableFrom(ctx.getClass())) {
            throw new IllegalArgumentException("applicationContext must be WebApplicationContext type");
        }

        String beanName = beanClass.getName();
        if (isController) {
            removeOldControllerMapping(beanName);
        }
        if (beanFactory.containsBean(beanName)) {
            beanFactory.destroySingleton(beanName); //移除单例bean
            removeInjectCache(beanName); //移除注入缓存 否则Caused by: java.lang.IllegalArgumentException: object is not an instance of declaring class
        }
        try {
            Object instance = beanClass.newInstance();
            beanFactory.registerSingleton(beanName, instance); //注册单例bean
            //beanFactory.autowireBean(instance); //自动注入
            //beanFactory.autowire(beanClass ,RootBeanDefinition.AUTOWIRE_BY_TYPE,true);
            beanFactory.autowireBeanProperties(instance, RootBeanDefinition.AUTOWIRE_BY_NAME, false);
            beanFactory.autowireBeanProperties(instance, RootBeanDefinition.AUTOWIRE_BY_TYPE, false);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (isController) {
            addControllerMapping(beanName);
        }
    }

    public void unregisterBean(Class<?> beanClass,boolean isController) throws IOException {
        Assert.notNull(beanClass, "register bean class must not null");
        if (!WebApplicationContext.class.isAssignableFrom(ctx.getClass())) {
            throw new IllegalArgumentException("applicationContext must be WebApplicationContext type");
        }

        String beanName = beanClass.getName();
        if (isController) {
            removeOldControllerMapping(beanName);
        }
        if (beanFactory.containsBean(beanName)) {
            beanFactory.destroySingleton(beanName); //移除单例bean
            removeInjectCache(beanName); //移除注入缓存 否则Caused by: java.lang.IllegalArgumentException: object is not an instance of declaring class
        }
    }

    public void registerGroovyBean(String scriptLocation, boolean isController) throws IOException {
        if (scriptNotExists(scriptLocation)) {
            throw new IllegalArgumentException("script not exists : " + scriptLocation);
        }
        scriptLastModifiedMap.put(scriptLocation, scriptLastModified(scriptLocation));

        // Create script factory bean definition.
        GroovyScriptFactory groovyScriptFactory = new GroovyScriptFactory(scriptLocation);
        groovyScriptFactory.setBeanFactory(beanFactory);
        groovyScriptFactory.setBeanClassLoader(beanFactory.getBeanClassLoader());

        Object controller =
                groovyScriptFactory.getScriptedObject(new ResourceScriptSource(ctx.getResource(scriptLocation)));

        String beanName = scriptLocation;
        if (isController) {
            removeOldControllerMapping(beanName);
        }
        if (beanFactory.containsBean(beanName)) {
            beanFactory.destroySingleton(beanName); //移除单例bean
            removeInjectCache(controller.getClass().getName()); //移除注入缓存 否则Caused by: java.lang.IllegalArgumentException: object is not an instance of declaring class
        }
        beanFactory.registerSingleton(beanName, controller); //注册单例bean
        beanFactory.autowireBean(controller); //自动注入
        if (isController) {
            addControllerMapping(beanName);
        }
    }

    public void unregisterGroovyBean(String scriptLocation, boolean isController) throws IOException {
        if (scriptNotExists(scriptLocation)) {
            throw new IllegalArgumentException("script not exists : " + scriptLocation);
        }
        scriptLastModifiedMap.put(scriptLocation, scriptLastModified(scriptLocation));

        // Create script factory bean definition.
        GroovyScriptFactory groovyScriptFactory = new GroovyScriptFactory(scriptLocation);
        groovyScriptFactory.setBeanFactory(beanFactory);
        groovyScriptFactory.setBeanClassLoader(beanFactory.getBeanClassLoader());
        Object controller =
                groovyScriptFactory.getScriptedObject(new ResourceScriptSource(ctx.getResource(scriptLocation)));

        String beanName = scriptLocation;
        if (isController) {
            removeOldControllerMapping(beanName);
        }
        if (beanFactory.containsBean(beanName)) {
            beanFactory.destroySingleton(beanName); //移除单例bean
            removeInjectCache(controller.getClass().getName()); //移除注入缓存 否则Caused by: java.lang.IllegalArgumentException: object is not an instance of declaring class
        }
    }

    public void registerGroovyBean(String scriptLocation) throws IOException {
        registerGroovyBean(scriptLocation, false);
    }

    public void registerGroovyController(String scriptLocation) throws IOException {
        registerGroovyBean(scriptLocation, true);
    }

    public void unregisterGroovyBean(String scriptLocation) throws IOException {
        unregisterGroovyBean(scriptLocation, false);
    }

    public void unregisterGroovyController(String scriptLocation) throws IOException {
        unregisterGroovyBean(scriptLocation, true);
    }

    private void removeOldControllerMapping(String controllerBeanName) {
        if (!beanFactory.containsBean(controllerBeanName)) {
            return;
        }
        RequestMappingHandlerMapping requestMappingHandlerMapping = requestMappingHandlerMapping();

        //remove old
        Class<?> handlerType = ctx.getType(controllerBeanName);
        final Class<?> userType = ClassUtils.getUserClass(handlerType);

        final RequestMappingHandlerMapping innerRequestMappingHandlerMapping = requestMappingHandlerMapping;
        Set<Method> methods = MethodIntrospector.selectMethods(userType, new ReflectionUtils.MethodFilter() {
            @Override
            public boolean matches(Method method) {
                return ReflectionUtils.invokeMethod(
                        getMappingForMethodMethod,
                        innerRequestMappingHandlerMapping,
                        method, userType) != null;
            }
        });

        for (Method method : methods) {
            RequestMappingInfo mapping =
                    (RequestMappingInfo) ReflectionUtils.invokeMethod(getMappingForMethodMethod, requestMappingHandlerMapping, method, userType);
            ReflectionUtils.invokeMethod(unregisterMappingMethod, requestMappingHandlerMapping, mapping);
        }
    }


    private void addControllerMapping(String controllerBeanName) {
        removeOldControllerMapping(controllerBeanName);
        RequestMappingHandlerMapping requestMappingHandlerMapping = requestMappingHandlerMapping();
        //spring 3.1 开始
        ReflectionUtils.invokeMethod(detectHandlerMethodsMethod, requestMappingHandlerMapping, controllerBeanName);
    }

    private RequestMappingHandlerMapping requestMappingHandlerMapping() {
        try {
            return ctx.getBean(RequestMappingHandlerMapping.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("applicationContext must has RequestMappingHandlerMapping");
        }
    }

    private void removeInjectCache(String beanName) {
        AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor =
                ctx.getBean(AutowiredAnnotationBeanPostProcessor.class);
        @SuppressWarnings("unchecked") Map<String, InjectionMetadata> injectionMetadataMap =
                (Map<String, InjectionMetadata>) ReflectionUtils.getField(injectionMetadataCacheField, autowiredAnnotationBeanPostProcessor);
        injectionMetadataMap.remove(beanName);
    }

    private void startScriptModifiedCheckThead(final Long scriptCheckInterval) {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(scriptCheckInterval);
                    Map<String, Long> copyMap = new HashMap<>(scriptLastModifiedMap);
                    for (String scriptLocation : copyMap.keySet()) {
                        if (scriptNotExists(scriptLocation)) {
                            scriptLastModifiedMap.remove(scriptLocation);
                            //TODO remove handler mapping ?
                        }
                        if (copyMap.get(scriptLocation) != scriptLastModified(scriptLocation)) {
                            registerGroovyController(scriptLocation);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //ignore
                }
            }
        }).start();
    }

    private long scriptLastModified(String scriptLocation) {
        try {
            return ctx.getResource(scriptLocation).getFile().lastModified();
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean scriptNotExists(String scriptLocation) {
        return !ctx.getResource(scriptLocation).exists();
    }
}