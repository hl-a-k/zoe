/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zoe.framework.data.jpa.repository.config;

import com.zoe.framework.data.jpa.repository.support.Sql2oJpaRepositoryFactoryBean;
import com.zoe.framework.data.jpa.repository.support.Sql2oRepository;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.util.StringUtils;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * JPA specific configuration extension parsing custom attributes from the XML namespace and
 * {@link EnableSql2oJpaRepositories} annotation. Also, it registers bean definitions for a
 * {@link //PersistenceAnnotationBeanPostProcessor} (to trigger injection into {@link PersistenceContext}/
 * {@link PersistenceUnit} annotated properties and methods) as well as
 * {@link PersistenceExceptionTranslationPostProcessor} to enable exception translation of persistence specific
 * exceptions into Spring's {@link DataAccessException} hierarchy.
 *
 * @author Oliver Gierke
 * @author Eberhard Wolff
 * @author Gil Markham
 * @author Thomas Darimont
 */
public class Sql2oJpaRepositoryConfigExtension extends RepositoryConfigurationExtensionSupport {

    private static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";
    private static final String ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE = "enableDefaultTransactions";

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getModuleName()
     */
    @Override
    public String getModuleName() {
        return "JPA";
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config14.RepositoryConfigurationExtension#getRepositoryInterface()
     */
    public String getRepositoryFactoryClassName() {
        return Sql2oJpaRepositoryFactoryBean.class.getName();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config14.RepositoryConfigurationExtensionSupport#getModulePrefix()
     */
    @Override
    protected String getModulePrefix() {
        return getModuleName().toLowerCase(Locale.US);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingAnnotations()
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Arrays.asList(Entity.class, MappedSuperclass.class);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#getIdentifyingTypes()
     */
    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Collections.singleton(Sql2oRepository.class);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.RepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {

        String transactionManagerRef = source.getAttribute("transactionManagerRef");
        builder.addPropertyValue("transactionManager",
                transactionManagerRef == null ? DEFAULT_TRANSACTION_MANAGER_BEAN_NAME : transactionManagerRef);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {

        AnnotationAttributes attributes = config.getAttributes();

        builder.addPropertyValue(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE,
                attributes.getBoolean(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport#postProcess(org.springframework.beans.factory.support.BeanDefinitionBuilder, org.springframework.data.repository.config.XmlRepositoryConfigurationSource)
     */
    @Override
    public void postProcess(BeanDefinitionBuilder builder, XmlRepositoryConfigurationSource config) {

        String enableDefaultTransactions = config.getAttribute(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE);

        if (StringUtils.hasText(enableDefaultTransactions)) {
            builder.addPropertyValue(ENABLE_DEFAULT_TRANSACTIONS_ATTRIBUTE, enableDefaultTransactions);
        }
    }
}
