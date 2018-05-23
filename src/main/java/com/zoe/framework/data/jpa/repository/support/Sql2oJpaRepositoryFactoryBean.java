package com.zoe.framework.data.jpa.repository.support;

import com.zoe.framework.data.auditing.AuditingHandler;
import com.zoe.framework.sql2o.Sql2o;
import com.zoe.framework.sql2o.datasource.DataSource;
import com.zoe.framework.sql2o.datasource.DynamicDataSourceRepositoryProxyPostProcessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

/**
 * SqlBag JpaRepositoryFactoryBean
 * Created by caizhicong on 2017/7/4.
 */
public class Sql2oJpaRepositoryFactoryBean<T extends Sql2oRepository<S, ID>, S, ID extends Serializable> extends TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    @Autowired
    private Sql2o sql2o;
    @Autowired
    private AuditingHandler auditingHandler;
    private RepositoryProxyPostProcessor dynamicDataSourceRepositoryProxyPostProcessor;
    private BeanFactory beanFactory;

    protected Sql2oJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        Assert.notNull(sql2o, "sql2o must not be null!");
        Assert.notNull(auditingHandler, "auditingHandler must not be null!");
        Class<? extends T> repositoryInterface = this.getObjectType();
        DataSource dataSource =  repositoryInterface.getAnnotation(DataSource.class);
        if(dataSource != null && !dataSource.sql2oBeanName().isEmpty()) {
            Sql2o sql2o = beanFactory.getBean(dataSource.sql2oBeanName(), Sql2o.class);
            return new Sql2oJpaRepositoryFactory(sql2o, auditingHandler);
        }

        Sql2oJpaRepositoryFactory repositoryFactory = new Sql2oJpaRepositoryFactory(sql2o, auditingHandler);
        //在这边添加拦截器只能拦截Repository类，造成外部Service类有@Transactional时，会引发bug
        if(dynamicDataSourceRepositoryProxyPostProcessor != null) {
            repositoryFactory.addRepositoryProxyPostProcessor(dynamicDataSourceRepositoryProxyPostProcessor);
        }
        return repositoryFactory;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        super.setBeanFactory(beanFactory);

        if(beanFactory.containsBean(DynamicDataSourceRepositoryProxyPostProcessor.BEAN_NAME)) {
            this.dynamicDataSourceRepositoryProxyPostProcessor = beanFactory.getBean(DynamicDataSourceRepositoryProxyPostProcessor.class);
        }
        if (sql2o == null) {
            sql2o = beanFactory.getBean(Sql2o.class);
        }
        if (auditingHandler == null) {
            auditingHandler = beanFactory.getBean(AuditingHandler.class);
        }
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        System.out.println(entityManager);
    }
}
