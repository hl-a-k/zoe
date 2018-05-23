package com.zoe.framework.sql2o.reflection;

import java.lang.reflect.Field;

/**
 * Created by caizhicong on 2016/10/3.
 *
 * @author mdelapenya
 * @author caizhicong
 */
public interface FieldMemberFactory {

    IMember newMember(Field field);
}
