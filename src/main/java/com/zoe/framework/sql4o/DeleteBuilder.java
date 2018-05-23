package com.zoe.framework.sql4o;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating SQL delete statements.
 *
 * @author John Krasnay <john@krasnay.ca>
 */
public class DeleteBuilder extends AbstractSqlBuilder implements Serializable {

    private static final long serialVersionUID = 1;

    private String table;

    private List<String> wheres = new ArrayList<String>();

    public DeleteBuilder(String table) {
        this.table = table;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("delete from ").append(table);
        appendList(sql, wheres, " where ", " and ");
        return sql.toString();
    }

    public DeleteBuilder where(String expr) {
        wheres.add(expr);
        return this;
    }

    public DeleteBuilder where(Predicate predicate) {
        predicate.init(this);
        where(predicate.toSql());
        return this;
    }

    public DeleteBuilder whereEquals(String column, Object value) {
        where(column + " = :" + column);
        setParameter(column, value);
        return this;
    }
}
