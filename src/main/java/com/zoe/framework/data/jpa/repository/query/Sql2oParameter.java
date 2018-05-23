package com.zoe.framework.data.jpa.repository.query;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.query.Parameter;

/**
 * Created by caizhicong on 2017/7/12.
 */
public class Sql2oParameter extends Parameter {
    /**
     * Creates a new {@link Parameter} for the given {@link MethodParameter}.
     *
     * @param parameter must not be {@literal null}.
     */
    protected Sql2oParameter(MethodParameter parameter) {
        super(parameter);
    }
}
