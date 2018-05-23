package com.zoe.framework.sql4o;

/**
 * SqlBuilder
 * Created by caizhicong on 2016/9/9.
 */
public final class SqlBuilder {

    public static SelectBuilder select(){
        return new SelectBuilder();
    }

    public static InsertBuilder insert(String table) {
        return new InsertBuilder(table);
    }

    public static UpdateBuilder update(String table){
        return new UpdateBuilder(table);
    }
}
