package com.zoe.framework.data.jpa.repository.query;

import com.zoe.framework.context.SpringContextHolder;
import com.zoe.framework.data.jpa.domain.AuditableEntity;
import com.zoe.framework.data.jpa.domain.ValidableEntity;
import com.zoe.framework.data.jpa.repository.support.QueryInfo;
import com.zoe.framework.data.jpa.repository.support.Sql2oCrudService;
import com.zoe.framework.data.jpa.repository.support.Sql2oRepository;
import com.zoe.framework.sql2o.data.PojoData;
import com.zoe.framework.sqlbag.SqlBag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.*;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Sql2oTemplateQuery
 * Created by caizhicong on 2017/7/10.
 */
public class Sql2oTemplateQuery implements RepositoryQuery {

    private final Sql2oQueryMethod method;
    private final Sql2oRepository repository;
    private String queryString;
    private SqlBag sqlBag;

    public Sql2oTemplateQuery(Sql2oQueryMethod method, Sql2oRepository repository) {
        this.queryString = method.getAnnotatedQuery();
        this.method = method;
        this.repository = repository;
    }

    /**
     * Executes the {@link RepositoryQuery} with the given parameters.
     *
     * @param parameters
     * @return
     */
    @Override
    public Object execute(Object[] parameters) {
        Class<?> domainClass = method.getReturnedObjectType();
        Class<?> returnType = method.getReturnType();
        boolean isMap = Map.class.isAssignableFrom(domainClass);

        LinkedCaseInsensitiveMap<Object> params = new LinkedCaseInsensitiveMap<>();
        Sql2oParameters parameterItems = this.method.getParameters();
        for (Parameter parameter : parameterItems) {
            String name = parameter.getName();
            if (name == null || !parameter.isBindable()) continue;
            params.put(name, parameters[parameter.getIndex()]);
        }
        boolean validable = ValidableEntity.class.isAssignableFrom(domainClass);
        if(validable && !method.checkValid()) {
            params.put("validFlag", "null");
        }

        String queryString = null;
        String queryName = method.getNamedQueryName();
        boolean isDynamicSql = false;
        if (!StringUtils.isEmpty(queryName)) {
            if (sqlBag == null) {
                synchronized (this) {
                    this.sqlBag = SpringContextHolder.getBean(SqlBag.class);
                    Assert.notNull(sqlBag, "sqlbag was not injected!!!");
                }
            }
            queryString = sqlBag.get(queryName, params);
            isDynamicSql = !StringUtils.isEmpty(queryString);
        }
        if (StringUtils.isEmpty(queryString)) {
            queryString = this.getQueryString();
        }

        ParameterAccessor accessor = new ParametersParameterAccessor(this.method.getParameters(), parameters);
        if (!isMap) {
            if (isQueryStringGenerated) {//自动生成的语法加上过滤
                queryString = Sql2oCrudService.instance().buildFilterSql(domainClass, queryString, params);
            }
            queryString = Sql2oQueryUtils.applySorting(queryString, accessor.getSort());
        } else if (!isDynamicSql) {
            queryString = Sql2oQueryUtils.applyFilters(queryString, params);
            queryString = Sql2oQueryUtils.applySorting(queryString, accessor.getSort());
        }

        Object result = null;
        if (method.isCollectionQuery()) {
            result = repository.findListBySql(domainClass, queryString, params);
        } else if (method.isModifyingQuery()) {
            result = repository.executeSql(queryString, params);
        } else if (method.isProcedureQuery()) {//todo 未经过验证，后续用到再验证
            result = repository.executeCall(queryString, returnType, parameters);
        } else if (method.isPageQuery()) {
            Pageable pageable = accessor.getPageable();
            if (pageable == null) {
                pageable = new QueryInfo();
            }
            QueryInfo queryInfo;
            if (pageable instanceof QueryInfo) {
                queryInfo = (QueryInfo) pageable;
            } else {
                queryInfo = new QueryInfo(pageable.getPageNumber(), pageable.getPageSize()).setSql(queryString).setQueryItems(params);
            }
            result = repository.findPage(domainClass, queryInfo);
        } else if (method.isQueryForEntity()) {
            boolean allNull = true;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (validable && entry.getValue() != null
                        && !entry.getKey().equalsIgnoreCase("validFlag")
                        && !entry.getKey().equalsIgnoreCase("valid_flag")) {
                    allNull = false;
                    break;
                }
            }
            if (allNull) {
                result = null;
            } else {
                result = repository.findBySql(domainClass, queryString, params);
            }
        }
        if (isMap) return result;

        ResultProcessor withDynamicProjection = method.getResultProcessor().withDynamicProjection(accessor);
        return withDynamicProjection.processResult(result);
    }

    /**
     * Returns the
     *
     * @return
     */
    @Override
    public Sql2oQueryMethod getQueryMethod() {
        return method;
    }

    private boolean isQueryStringGenerated = false;

    public String getQueryString() {
        if (StringUtils.isEmpty(queryString)) {
            Class<?> domainClass = method.getEntityInformation().getJavaType();
            String tableName = PojoData.forClass(domainClass).getTableInfo().getTableName();
            String methodName = method.getName();
            if (methodName.startsWith("deleteBy")) {
                queryString = "delete from " + tableName;
            } else {
                queryString = "select * from " + tableName;
            }
            isQueryStringGenerated = true;
        }
        return queryString;
    }
}
