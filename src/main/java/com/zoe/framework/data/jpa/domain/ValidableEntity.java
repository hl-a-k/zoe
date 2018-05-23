package com.zoe.framework.data.jpa.domain;

import org.springframework.data.domain.Persistable;

import java.io.Serializable;

/**
 * 有效性实体接口
 * Created by caizhicong on 2017/11/6.
 */
public interface ValidableEntity<ID extends Serializable> extends Persistable<ID> {

    int VALID = 1;

    int NOT_VALID = 0;

    int DELETED = -1;

    String FieldName = "validFlag";

    Integer getValidFlag();

    void setValidFlag(Integer validFlag);
}
