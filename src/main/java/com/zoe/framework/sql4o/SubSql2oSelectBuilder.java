package com.zoe.framework.sql4o;

import java.io.Serializable;


/**
 * Creator for part of a SQL sub-select statement used as a column expression or a FROM clause.
 * You shouldn't create these directly. Instead, acquire one from the {@link Sql2oSelectBuilder#subSelectColumn(String)} method.
 *
 * @author John Krasnay <john@krasnay.ca>
 */
public class SubSql2oSelectBuilder implements Serializable {

    private static final long serialVersionUID = 1;

    /**
     * Builder that builds this select.
     */
    private SubSelectBuilder builder;

    /**
     * Owning select creator. Parameters are stored here.
     */
    private Sql2oSelectBuilder creator;

    SubSql2oSelectBuilder(Sql2oSelectBuilder creator, SubSelectBuilder builder) {
        this.builder = builder;
        this.creator = creator;
    }

    /**
     * Copy constructor. Used by {@link #clone()}.
     *
     * @param owner
     *            Sql2oSelectBuilder that owns the new UnionSql2oSelectBuilder
     * @param other
     *            UnionSql2oSelectBuilder being cloned.
     */
    protected SubSql2oSelectBuilder(Sql2oSelectBuilder owner, SubSql2oSelectBuilder other) {
        this.builder = other.builder.clone();
        this.creator = owner;
    }

    public SubSql2oSelectBuilder and(Predicate predicate) {
        predicate.init(creator.getBuilder());
        builder.where(predicate.toSql());
        return this;
    }

    public SubSql2oSelectBuilder and(String expr) {
        builder.and(expr);
        return this;
    }

    public SubSql2oSelectBuilder clone(Sql2oSelectBuilder owner) {
        return new SubSql2oSelectBuilder(owner, this);
    }

    public SubSql2oSelectBuilder column(String name) {
        builder.column(name);
        return this;
    }

    public SubSql2oSelectBuilder column(String name, boolean groupBy) {
        builder.column(name, groupBy);
        return this;
    }

    public SubSql2oSelectBuilder distinct() {
        builder.distinct();
        return this;
    }

    public SubSql2oSelectBuilder from(String table) {
        builder.from(table);
        return this;
    }

    public SubSql2oSelectBuilder groupBy(String expr) {
        builder.groupBy(expr);
        return this;
    }

    public SubSql2oSelectBuilder having(String expr) {
        builder.having(expr);
        return this;
    }

    public SubSql2oSelectBuilder join(String join) {
        builder.join(join);
        return this;
    }

    public SubSql2oSelectBuilder leftJoin(String join) {
        builder.leftJoin(join);
        return this;
    }

    public SubSql2oSelectBuilder setParameter(String name, Object value) {
        creator.getBuilder().setParameter(name, value);
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public SubSql2oSelectBuilder where(Predicate predicate) {
        predicate.init(creator.getBuilder());
        builder.where(predicate.toSql());
        return this;
    }

    public SubSql2oSelectBuilder where(String expr) {
        builder.where(expr);
        return this;
    }

}