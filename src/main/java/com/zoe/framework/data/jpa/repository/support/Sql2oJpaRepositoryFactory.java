package com.zoe.framework.data.jpa.repository.support;

import com.zoe.framework.data.auditing.AuditingHandler;
import com.zoe.framework.data.jpa.repository.query.Sql2oQueryLookupStrategy;
import com.zoe.framework.sql2o.Sql2o;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.io.Serializable;

/**
 * Sql2oJpaRepositoryFactory
 * Created by caizhicong on 2017/7/5.
 */
public class Sql2oJpaRepositoryFactory<T extends Persistable, ID extends Serializable> extends RepositoryFactorySupport {

    private final Sql2o sql2o;
    private final AuditingHandler auditingHandler;

    public Sql2oJpaRepositoryFactory(Sql2o sql2o, AuditingHandler auditingHandler) {
        this.sql2o = sql2o;
        this.auditingHandler = auditingHandler;
    }

    @Override
    public <TEntity , TID extends Serializable> EntityInformation<TEntity, TID> getEntityInformation(Class<TEntity> aClass) {
        return new Sql2oJpaEntityInformation<>(aClass);
    }

    protected Object getTargetRepository(RepositoryInformation information) {
        //noinspection unchecked
        Sql2oJpaRepository repository = new Sql2oJpaRepository<T, ID>((Class<T>) information.getDomainType(), sql2o, auditingHandler);
        return repository;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
        return Sql2oJpaRepository.class;
    }

    @Override
    protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider) {
        Sql2oJpaRepository repository = new Sql2oJpaRepository<>(Persistable.class, sql2o, auditingHandler);
        return new Sql2oQueryLookupStrategy(repository);
    }
}