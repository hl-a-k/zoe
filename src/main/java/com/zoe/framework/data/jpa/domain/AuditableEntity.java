package com.zoe.framework.data.jpa.domain;

import org.springframework.data.domain.Persistable;

import java.io.Serializable;
import java.util.Date;

/**
 * 可审计实体接口
 * Created by caizhicong on 2017/11/6.
 */
public interface AuditableEntity<U, ID extends Serializable> extends Persistable<ID> {

    void setId(ID id);

    U getCreateUser();

    void setCreateUser(U createUser);

    Date getCreateTime();

    void setCreateTime(Date createTime);

    U getModifyUser();

    void setModifyUser(U modifyUser);

    Date getModifyTime();

    void setModifyTime(Date modifyTime);
}
