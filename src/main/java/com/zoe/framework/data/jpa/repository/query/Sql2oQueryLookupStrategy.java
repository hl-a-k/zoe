package com.zoe.framework.data.jpa.repository.query;

import com.zoe.framework.data.jpa.repository.support.Sql2oRepository;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

/**
 * Created by caizhicong on 2017/7/10.
 */
public class Sql2oQueryLookupStrategy implements QueryLookupStrategy {

    private final Sql2oRepository repository;

    public Sql2oQueryLookupStrategy(Sql2oRepository repository) {
        this.repository = repository;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                        NamedQueries namedQueries) {
        return new Sql2oTemplateQuery(new Sql2oQueryMethod(method, metadata, factory), repository);
    }
}