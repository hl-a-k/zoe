package com.zoe.framework.sql4o;

import java.io.Serializable;
import java.util.*;

/**
 * Tool for programmatically constructing SQL select statements. This class aims
 * to simplify the task of juggling commas and SQL keywords when building SQL
 * statements from scratch, but doesn't attempt to do much beyond that. Here are
 * some relatively complex examples:
 * <p>
 * <pre>
 * String sql = new SelectBuilder()
 * .column(&quot;e.id&quot;)
 * .column(&quot;e.name as empname&quot;)
 * .column(&quot;d.name as deptname&quot;)
 * .column(&quot;e.salary&quot;)
 * .from((&quot;Employee e&quot;)
 * .join(&quot;Department d on e.dept_id = d.id&quot;)
 * .where(&quot;e.salary &gt; 100000&quot;)
 * .orderBy(&quot;e.salary desc&quot;)
 * .toString();
 * </pre>
 * <p>
 * <pre>
 * String sql = new SelectBuilder()
 * .column(&quot;d.id&quot;)
 * .column(&quot;d.name&quot;)
 * .column(&quot;sum(e.salary) as total&quot;)
 * .from(&quot;Department d&quot;)
 * .join(&quot;Employee e on e.dept_id = d.id&quot;)
 * .groupBy(&quot;d.id&quot;)
 * .groupBy(&quot;d.name&quot;)
 * .having(&quot;total &gt; 1000000&quot;).toString();
 * </pre>
 * <p>
 * Note that the methods can be called in any order. This is handy when a base
 * class wants to create a simple query but allow subclasses to augment it.
 * <p>
 * It's similar to the Squiggle SQL library
 * (http://code.google.com/p/squiggle-sql/), but makes fewer assumptions about
 * the internal structure of the SQL statement, which I think makes for simpler,
 * cleaner code. For example, in Squiggle you would write...
 * <p>
 * <pre>
 * select.addCriteria(new MatchCriteria(orders, &quot;status&quot;, MatchCriteria.EQUALS, &quot;processed&quot;));
 * </pre>
 * <p>
 * With SelectBuilder, we assume you know how to write SQL expressions, so
 * instead you would write...
 * <p>
 * <pre>
 * select.where(&quot;status = 'processed'&quot;);
 * </pre>
 *
 * @author John Krasnay <john@krasnay.ca>
 */
public class SelectBuilder extends AbstractSqlBuilder implements Cloneable, Serializable {

    private static final long serialVersionUID = 1;

    private boolean distinct;

    private List<Object> columns = new ArrayList<Object>();

    private List<String> tables = new ArrayList<String>();

    private List<String> joins = new ArrayList<String>();

    private List<String> leftJoins = new ArrayList<String>();

    private List<String> wheres = new ArrayList<String>();

    private List<String> groupBys = new ArrayList<String>();

    private List<String> havings = new ArrayList<String>();

    private List<SelectBuilder> unions = new ArrayList<SelectBuilder>();

    private List<String> orderBys = new ArrayList<String>();

    private boolean forUpdate;

    private boolean noWait;

    public SelectBuilder() {

    }

    public SelectBuilder(String table) {
        tables.add(table);
    }

    /**
     * Copy constructor. Used by {@link #clone()}.
     *
     * @param other SelectBuilder being cloned.
     */
    protected SelectBuilder(SelectBuilder other) {

        this.distinct = other.distinct;
        this.forUpdate = other.forUpdate;
        this.noWait = other.noWait;

        for (Object column : other.columns) {
            if (column instanceof SubSelectBuilder) {
                this.columns.add(((SubSelectBuilder) column).clone());
            } else {
                this.columns.add(column);
            }
        }

        this.tables.addAll(other.tables);
        this.joins.addAll(other.joins);
        this.leftJoins.addAll(other.leftJoins);
        this.wheres.addAll(other.wheres);
        this.groupBys.addAll(other.groupBys);
        this.havings.addAll(other.havings);

        for (SelectBuilder sb : other.unions) {
            this.unions.add(sb.clone());
        }

        this.orderBys.addAll(other.orderBys);
    }

    public static SelectBuilder newInstance(){
        return new SelectBuilder();
    }

    /**
     * Alias for {@link #where(String)}.
     */
    public SelectBuilder and(String expr) {
        return where(expr);
    }

    public SelectBuilder select(String... names) {
        Collections.addAll(columns, names);
        return this;
    }

    public SelectBuilder column(String name) {
        columns.add(name);
        return this;
    }

    public SelectBuilder columns(String... names) {
        Collections.addAll(columns, names);
        return this;
    }

    public SelectBuilder column(SubSelectBuilder subSelect) {
        columns.add(subSelect);
        return this;
    }

    public SelectBuilder subSelectColumn(String alias) {
        SubSelectBuilder subSelectBuilder = new SubSelectBuilder(alias);
        column(subSelectBuilder);
        return new SelectBuilder(subSelectBuilder);
    }

    public SelectBuilder column(String name, boolean groupBy) {
        columns.add(name);
        if (groupBy) {
            groupBys.add(name);
        }
        return this;
    }

    @Override
    public SelectBuilder clone() {
        return new SelectBuilder(this);
    }

    public SelectBuilder distinct() {
        this.distinct = true;
        return this;
    }

    public SelectBuilder forUpdate() {
        forUpdate = true;
        return this;
    }

    public SelectBuilder from(String table) {
        tables.add(table);
        return this;
    }

    public SelectBuilder from(String table, String alias) {
        tables.add(table + appendAlias(alias));
        return this;
    }

    public List<SelectBuilder> getUnions() {
        return unions;
    }

    public SelectBuilder groupBy(String expr) {
        groupBys.add(expr);
        return this;
    }

    public SelectBuilder groupBys(String... names) {
        for (String name : names) {
            groupBys.add(name);
        }
        return this;
    }

    public SelectBuilder having(String expr) {
        havings.add(expr);
        return this;
    }

    public SelectBuilder join(String join) {
        joins.add(join);
        return this;
    }

    public SelectBuilder leftJoin(String join) {
        leftJoins.add(join);
        return this;
    }

    public SelectBuilder leftJoin(String table, String alias, String left, String right) {
        leftJoins.add(table + appendAlias(alias) + " on " + left + " = " + right);
        return this;
    }

    public SelectBuilder noWait() {
        if (!forUpdate) {
            throw new RuntimeException("noWait without forUpdate cannot be called");
        }
        noWait = true;
        return this;
    }

    public SelectBuilder orderBy(String name) {
        orderBys.add(name);
        return this;
    }

    public SelectBuilder orderBys(String... names) {
        for (String name : names) {
            orderBys.add(name);
        }
        return this;
    }

    /**
     * Adds an ORDER BY item with a direction indicator.
     *
     * @param name      Name of the column by which to sort.
     * @param ascending If true, specifies the direction "asc", otherwise, specifies
     *                  the direction "desc".
     */
    public SelectBuilder orderBy(String name, boolean ascending) {
        if (ascending) {
            orderBys.add(name + " asc");
        } else {
            orderBys.add(name + " desc");
        }
        return this;
    }

    @Override
    public String toString() {

        StringBuilder sql = new StringBuilder("select ");

        if (distinct) {
            sql.append("distinct ");
        }

        if (columns.size() == 0) {
            sql.append("*");
        } else {
            appendList(sql, columns, "", ", ");
        }

        appendList(sql, tables, " from ", ", ");
        appendList(sql, joins, " join ", " join ");
        appendList(sql, leftJoins, " left join ", " left join ");
        appendList(sql, wheres, " where ", " and ");
        appendList(sql, groupBys, " group by ", ", ");
        appendList(sql, havings, " having ", " and ");
        appendList(sql, unions, " union ", " union ");
        appendList(sql, orderBys, " order by ", ", ");

        if (forUpdate) {
            sql.append(" for update");
            if (noWait) {
                sql.append(" nowait");
            }
        }

        return sql.toString();
    }

    /**
     * Adds a "union" select builder. The generated SQL will union this query
     * with the result of the main query. The provided builder must have the
     * same columns as the parent select builder and must not use "order by" or
     * "for update".
     */
    public SelectBuilder union(SelectBuilder unionBuilder) {
        unions.add(unionBuilder);
        return this;
    }

    public SelectBuilder where(String expr) {
        wheres.add(expr);
        return this;
    }

    public SelectBuilder and(Predicate predicate) {
        return where(predicate);
    }

    public SelectBuilder where(Predicate predicate) {
        predicate.init(this);
        where(predicate.toSql());
        return this;
    }

    public SelectBuilder whereEquals(String column, Object value) {
        where(column + " = :" + column);
        setParameter(column, value);
        return this;
    }

    public SelectBuilder whereIn(String column, List<?> values) {
        StringBuilder sb = new StringBuilder();
        sb.append(column).append(" in (");

        boolean first = true;
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            String param = allocateParameter(column);
            setParameter(param, value);
            if (!first) {
                sb.append(", ");
            }
            sb.append(":").append(param);
            first = false;
        }
        sb.append(")");
        where(sb.toString());
        return this;
    }
}
