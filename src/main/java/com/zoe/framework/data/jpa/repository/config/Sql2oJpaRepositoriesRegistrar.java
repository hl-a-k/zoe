package com.zoe.framework.data.jpa.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Sql2oJpaRepositoriesRegistrar
 * Created by caizhicong on 2018/2/9.
 */
public class Sql2oJpaRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableSql2oJpaRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new Sql2oJpaRepositoryConfigExtension();
    }
}
