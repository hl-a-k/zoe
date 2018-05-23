package com.zoe.framework.data.jpa.repository.support;

import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.support.AbstractEntityInformation;

import java.io.Serializable;

/**
 * Created by caizhicong on 2017/7/5.
 */
public class Sql2oJpaEntityInformation <T, ID extends Serializable> extends AbstractEntityInformation<T, ID> implements EntityInformation<T, ID> {

    public Sql2oJpaEntityInformation(Class<T> domainClass) {
        super(domainClass);
    }

    @Override
    public ID getId(T t) {
        return null;
    }

    @Override
    public Class<ID> getIdType() {
        return null;
    }
}
