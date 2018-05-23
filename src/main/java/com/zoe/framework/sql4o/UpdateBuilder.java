package com.zoe.framework.sql4o;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating SQL update statements.
 *
 * @author John Krasnay <john@krasnay.ca>
 */
public class UpdateBuilder extends AbstractSqlBuilder implements Serializable {

    private static final long serialVersionUID = 1;

    private String table;

    private List<String> sets = new ArrayList<String>();

    private List<String> wheres = new ArrayList<String>();

    public UpdateBuilder(String table) {
        this.table = table;
    }

    public UpdateBuilder set(String expr) {
        sets.add(expr);
        return this;
    }

    public UpdateBuilder set(String column, Object value) {
        set(column + " = :" + column);
        setParameter(column, value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("update ").append(table);
        appendList(sql, sets, " set ", ", ");
        appendList(sql, wheres, " where ", " and ");
        return sql.toString();
    }

    public UpdateBuilder where(String expr) {
        wheres.add(expr);
        return this;
    }

    public UpdateBuilder where(Predicate predicate) {
        predicate.init(this);
        where(predicate.toSql());
        return this;
    }

    public UpdateBuilder whereEquals(String column, Object value) {
        where(column + " = :" + column);
        setParameter(column, value);
        return this;
    }
}
