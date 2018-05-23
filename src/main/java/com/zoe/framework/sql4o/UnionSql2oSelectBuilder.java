package com.zoe.framework.sql4o;

import java.io.Serializable;

/**
 * Creator for part of a SQL select statement that comes after the UNION keyword.
 * You shouldn't create these directly. Instead, acquire one from the {@link Sql2oSelectBuilder#union(Sql2oSelectBuilder)} ()} method.
 *
 * @author John Krasnay <john@krasnay.ca>
 */
public class UnionSql2oSelectBuilder implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * Builder that builds this select.
     */
    private SelectBuilder builder;

    /**
     * Owning select creator. Parameters are stored here.
     */
    private Sql2oSelectBuilder creator;

    UnionSql2oSelectBuilder(Sql2oSelectBuilder creator, SelectBuilder builder) {
        this.builder = builder;
        this.creator = creator;
    }

    /**
     * Copy constructor. Used by {@link #clone()}.
     *
     * @param owner
     *            SelectCreator that owns the new UnionSql2oSelectBuilder
     * @param other
     *            UnionSql2oSelectBuilder being cloned.
     */
    protected UnionSql2oSelectBuilder(Sql2oSelectBuilder owner, UnionSql2oSelectBuilder other) {
        this.builder = other.builder.clone();
        this.creator = owner;
    }

    public UnionSql2oSelectBuilder clone(Sql2oSelectBuilder owner) {
        return new UnionSql2oSelectBuilder(owner, this);
    }

    public UnionSql2oSelectBuilder and(String expr) {
        builder.and(expr);
        return this;
    }

    public UnionSql2oSelectBuilder column(String name) {
        builder.column(name);
        return this;
    }

    public UnionSql2oSelectBuilder column(String name, boolean groupBy) {
        builder.column(name, groupBy);
        return this;
    }

    public UnionSql2oSelectBuilder distinct() {
        builder.distinct();
        return this;
    }

    public UnionSql2oSelectBuilder from(String table) {
        builder.from(table);
        return this;
    }

    public UnionSql2oSelectBuilder groupBy(String expr) {
        builder.groupBy(expr);
        return this;
    }

    public UnionSql2oSelectBuilder having(String expr) {
        builder.having(expr);
        return this;
    }

    public UnionSql2oSelectBuilder join(String join) {
        builder.join(join);
        return this;
    }

    public UnionSql2oSelectBuilder leftJoin(String join) {
        builder.leftJoin(join);
        return this;
    }

    public UnionSql2oSelectBuilder setParameter(String name, Object value) {
        creator.getBuilder().setParameter(name, value);
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public UnionSql2oSelectBuilder where(String expr) {
        builder.where(expr);
        return this;
    }

    public UnionSql2oSelectBuilder whereEquals(String expr, Object value) {

        String param = creator.getBuilder().allocateParameter();

        builder.where(expr + " = :" + param);
        creator.getBuilder().setParameter(param, value);

        return this;
    }
}
