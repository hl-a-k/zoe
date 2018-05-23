package com.zoe.framework.data.jpa.repository.query;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by caizhicong on 2017/7/12.
 */
public class Sql2oParameters extends Parameters<Sql2oParameters, Parameter> {

    private int pageableIndex;

    public Sql2oParameters(Method method) {
        super(method);
        pageableIndex = super.getPageableIndex();
        if (pageableIndex == -1) {
            List<Class<?>> types = Arrays.asList(method.getParameterTypes());
            for (int i = 0; i < types.size(); i++) {
                if (Pageable.class.isAssignableFrom(types.get(i))) {
                    pageableIndex = i;
                }
            }
        }
    }

    private Sql2oParameters(List<Parameter> parameters) {
        super(parameters);

        pageableIndex = super.getPageableIndex();
    }

    @Override
    protected Parameter createParameter(MethodParameter parameter) {
        return new Sql2oParameter(parameter);
    }

    @Override
    protected Sql2oParameters createFrom(List<Parameter> parameters) {
        return new Sql2oParameters(parameters);
    }

    public boolean hasPageableParameter() {
        return pageableIndex != -1;
    }

    public int getPageableIndex() {
        return pageableIndex;
    }
}
