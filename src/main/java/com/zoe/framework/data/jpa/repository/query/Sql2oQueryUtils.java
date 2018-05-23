package com.zoe.framework.data.jpa.repository.query;


import com.zoe.framework.data.jpa.repository.support.QueryInfo;
import com.zoe.framework.data.jpa.repository.support.Sql2oCrudService;
import com.zoe.framework.sql2o.data.PocoColumn;
import com.zoe.framework.sql2o.data.PojoData;
import com.zoe.framework.sql2o.data.TableInfo;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

/**
 * Simple utility class to create JPA queries.
 *
 * @author Oliver Gierke
 * @author Kevin Raymond
 * @author Thomas Darimont
 * @author Komi Innocent
 * @author Christoph Strobl
 * @author Mark Paluch
 */
public abstract class Sql2oQueryUtils {

    public static final String COUNT_QUERY_STRING = "select count(%s) from %s x";
    public static final String DELETE_ALL_QUERY_STRING = "delete from %s x";

    private static final String COUNT_REPLACEMENT_TEMPLATE = "select count(%s) $5$6$7";
    private static final String SIMPLE_COUNT_VALUE = "$2";
    private static final String COMPLEX_COUNT_VALUE = "$3$6";
    private static final String ORDER_BY_PART = "(?iu)\\s+order\\s+by\\s+.*$";

    private static final Pattern ALIAS_MATCH;
    private static final Pattern COUNT_MATCH;
    private static final Pattern PROJECTION_CLAUSE = Pattern.compile("select\\s+(.+)\\s+from");

    private static final Pattern NO_DIGITS = Pattern.compile("\\D+");
    private static final String IDENTIFIER = "[\\p{Lu}\\P{InBASIC_LATIN}\\p{Alnum}._$]+";
    private static final String IDENTIFIER_GROUP = String.format("(%s)", IDENTIFIER);

    private static final String JOIN = "join\\s+(fetch\\s+)?" + IDENTIFIER + "\\s+(as\\s+)?" + IDENTIFIER_GROUP;
    private static final Pattern JOIN_PATTERN = Pattern.compile(JOIN, Pattern.CASE_INSENSITIVE);

    private static final String EQUALS_CONDITION_STRING = "%s.%s = :%s";
    private static final Pattern ORDER_BY = Pattern.compile(".*order\\s+by\\s+.*", CASE_INSENSITIVE);
    private static final Pattern WHERE = Pattern.compile(".*where\\s+.*", CASE_INSENSITIVE);

    private static final Pattern NAMED_PARAMETER = Pattern.compile(":" + IDENTIFIER + "|\\#" + IDENTIFIER,
            CASE_INSENSITIVE);

    private static final Pattern CONSTRUCTOR_EXPRESSION;

    private static final int QUERY_JOIN_ALIAS_GROUP_INDEX = 3;
    private static final int VARIABLE_NAME_GROUP_INDEX = 4;

    private static final Pattern PUNCTATION_PATTERN = Pattern.compile(".*((?![\\._])[\\p{Punct}|\\s])");
    private static final Pattern FUNCTION_PATTERN;

    private static final String UNSAFE_PROPERTY_REFERENCE = "Sort expression '%s' must only contain property references or "
            + "aliases used in the select clause. If you really want to use something other than that for sorting, please use "
            + "JpaSort.unsafe(…)!";

    static {

        StringBuilder builder = new StringBuilder();
        builder.append("(?<=from)"); // from as starting delimiter
        builder.append("(?:\\s)+"); // at least one space separating
        builder.append(IDENTIFIER_GROUP); // Entity name, can be qualified (any
        builder.append("(?:\\sas)*"); // exclude possible "as" keyword
        builder.append("(?:\\s)+"); // at least one space separating
        builder.append("(?!(?:where))(\\w*)"); // the actual alias

        ALIAS_MATCH = compile(builder.toString(), CASE_INSENSITIVE);

        builder = new StringBuilder();
        builder.append("(select\\s+((distinct )?(.+?)?)\\s+)?(from\\s+");
        builder.append(IDENTIFIER);
        builder.append("(?:\\s+as)?\\s+)");
        builder.append(IDENTIFIER_GROUP);
        builder.append("(.*)");

        COUNT_MATCH = compile(builder.toString(), CASE_INSENSITIVE);


        builder = new StringBuilder();
        builder.append("select");
        builder.append("\\s+"); // at least one space separating
        builder.append("(.*\\s+)?"); // anything in between (e.g. distinct) at least one space separating
        builder.append("new");
        builder.append("\\s+"); // at least one space separating
        builder.append(IDENTIFIER);
        builder.append("\\s*"); // zero to unlimited space separating
        builder.append("\\(");
        builder.append(".*");
        builder.append("\\)");

        CONSTRUCTOR_EXPRESSION = compile(builder.toString(), CASE_INSENSITIVE + DOTALL);

        builder = new StringBuilder();
        builder.append("\\s+"); // at least one space
        builder.append("\\w+\\([0-9a-zA-z\\._,\\s']+\\)"); // any function call including parameters within the brackets
        builder.append("\\s+[as|AS]+\\s+(([\\w\\.]+))"); // the potential alias

        FUNCTION_PATTERN = compile(builder.toString());
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private Sql2oQueryUtils() {

    }

    /**
     * Returns the query string to execute an exists query for the given id attributes.
     *
     * @param entityName the name of the entity to create the query for, must not be {@literal null}.
     * @param countQueryPlaceHolder the placeholder for the count clause, must not be {@literal null}.
     * @param idAttributes the id attributes for the entity, must not be {@literal null}.
     * @return
     */
    public static String getExistsQueryString(String entityName, String countQueryPlaceHolder,
                                              Iterable<String> idAttributes) {

        StringBuilder sb = new StringBuilder(String.format(COUNT_QUERY_STRING, countQueryPlaceHolder, entityName));
        sb.append(" WHERE ");

        for (String idAttribute : idAttributes) {
            sb.append(String.format(EQUALS_CONDITION_STRING, "x", idAttribute, idAttribute));
            sb.append(" AND ");
        }

        sb.append("1 = 1");
        return sb.toString();
    }

    /**
     * Returns the query string for the given class name.
     *
     * @param template
     * @param entityName
     * @return
     */
    public static String getQueryString(String template, String entityName) {

        Assert.hasText(entityName, "Entity name must not be null or empty!");

        return String.format(template, entityName);
    }

    /**
     * Adds {@literal order by} clause to the JPQL query. Uses the DEFAULT_ALIAS to bind the sorting property to.
     *
     * @param query
     * @param params
     * @return
     */
    public static String applyFilters(String query, Map<String, Object> params) {
        Assert.hasText(query, "Query must not be null or empty!");

        if (null == params || params.size() == 0) {
            return query;
        }

        return Sql2oCrudService.instance().buildFilterSql(query, params);
    }

    /**
     * Adds {@literal order by} clause to the JPQL query. Uses the DEFAULT_ALIAS to bind the sorting property to.
     *
     * @param query
     * @param sort
     * @return
     */
    public static String applySorting(String query, Sort sort) {
        return applySorting(query, sort, detectAlias(query));
    }

    /**
     * Adds {@literal order by} clause to the JPQL query.
     *
     * @param query must not be {@literal null} or empty.
     * @param sort
     * @param alias
     * @return
     */
    public static String applySorting(String query, Sort sort, String alias) {

        Assert.hasText(query, "Query must not be null or empty!");

        if (null == sort || !sort.iterator().hasNext()) {
            return query;
        }

        StringBuilder builder = new StringBuilder(query);

        if (!ORDER_BY.matcher(query).matches()) {
            builder.append(" order by ");
        } else {
            builder.append(", ");
        }

        Set<String> aliases = getOuterJoinAliases(query);
        Set<String> functionAliases = getFunctionAliases(query);

        for (Order order : sort) {
            builder.append(getOrderClause(aliases, functionAliases, alias, order)).append(", ");
        }

        builder.delete(builder.length() - 2, builder.length());

        return builder.toString();
    }

    /**
     * Returns the order clause for the given {@link Order}. Will prefix the clause with the given alias if the referenced
     * property refers to a join alias, i.e. starts with {@code $alias.}.
     *
     * @param joinAliases the join aliases of the original query.
     * @param alias the alias for the root entity.
     * @param order the order object to build the clause for.
     * @return
     */
    private static String getOrderClause(Set<String> joinAliases, Set<String> functionAlias, String alias, Order order) {

        String property = order.getProperty();

        checkSortExpression(order);

        if (functionAlias.contains(property)) {
            return String.format("%s %s", property, toJpaDirection(order));
        }

        boolean qualifyReference = !property.contains("("); // ( indicates a function

        for (String joinAlias : joinAliases) {
            if (property.startsWith(joinAlias.concat("."))) {
                qualifyReference = false;
                break;
            }
        }

        String reference = qualifyReference && StringUtils.hasText(alias) ? String.format("%s.%s", alias, property)
                : property;
        String wrapped = order.isIgnoreCase() ? String.format("lower(%s)", reference) : reference;

        return String.format("%s %s", wrapped, toJpaDirection(order));
    }

    /**
     * Returns the aliases used for {@code left (outer) join}s.
     *
     * @param query
     * @return
     */
    private static Set<String> getOuterJoinAliases(String query) {

        Set<String> result = new HashSet<String>();
        Matcher matcher = JOIN_PATTERN.matcher(query);

        while (matcher.find()) {

            String alias = matcher.group(QUERY_JOIN_ALIAS_GROUP_INDEX);
            if (StringUtils.hasText(alias)) {
                result.add(alias);
            }
        }

        return result;
    }

    /**
     * Returns the aliases used for aggregate functions like {@code SUM, COUNT, ...}.
     *
     * @param query
     * @return
     */
    private static Set<String> getFunctionAliases(String query) {

        Set<String> result = new HashSet<String>();
        Matcher matcher = FUNCTION_PATTERN.matcher(query);

        while (matcher.find()) {

            String alias = matcher.group(1);

            if (StringUtils.hasText(alias)) {
                result.add(alias);
            }
        }

        return result;
    }

    private static String toJpaDirection(Order order) {
        return order.getDirection().name().toLowerCase(Locale.US);
    }

    /**
     * Resolves the alias for the entity to be retrieved from the given JPA query.
     *
     * @param query
     * @return
     */
    public static String detectAlias(String query) {

        Matcher matcher = ALIAS_MATCH.matcher(query);

        return matcher.find() ? matcher.group(2) : null;
    }

    /**
     * Creates a where-clause referencing the given entities and appends it to the given query string. Binds the given
     * entities to the query.
     *
     * @param <T>
     * @param queryString must not be {@literal null}.
     * @param entities must not be {@literal null}.
     * @param entityManager must not be {@literal null}.
     * @return
     */
    public static <T> Query applyAndBind(String queryString, Iterable<T> entities, EntityManager entityManager) {

        Assert.notNull(queryString, "Querystring must not be null!");
        Assert.notNull(entities, "Iterable of entities must not be null!");
        Assert.notNull(entityManager, "EntityManager must not be null!");

        Iterator<T> iterator = entities.iterator();

        if (!iterator.hasNext()) {
            return entityManager.createQuery(queryString);
        }

        String alias = detectAlias(queryString);
        StringBuilder builder = new StringBuilder(queryString);
        builder.append(" where");

        int i = 0;

        while (iterator.hasNext()) {

            iterator.next();

            builder.append(String.format(" %s = ?%d", alias, ++i));

            if (iterator.hasNext()) {
                builder.append(" or");
            }
        }

        Query query = entityManager.createQuery(builder.toString());

        iterator = entities.iterator();
        i = 0;

        while (iterator.hasNext()) {
            query.setParameter(++i, iterator.next());
        }

        return query;
    }

    /**
     * Creates a count projected query from the given original query.
     *
     * @param originalQuery must not be {@literal null} or empty.
     * @return
     */
    public static String createCountQueryFor(String originalQuery) {
        return createCountQueryFor(originalQuery, null);
    }

    /**
     * Creates a count projected query from the given original query.
     *
     * @param originalQuery must not be {@literal null}.
     * @param countProjection may be {@literal null}.
     * @return
     * @since 1.6
     */
    public static String createCountQueryFor(String originalQuery, String countProjection) {

        Assert.hasText(originalQuery, "OriginalQuery must not be null or empty!");

        Matcher matcher = COUNT_MATCH.matcher(originalQuery);
        String countQuery;

        if (countProjection == null) {

            String variable = matcher.matches() ? matcher.group(VARIABLE_NAME_GROUP_INDEX) : null;
            boolean useVariable = variable != null && StringUtils.hasText(variable) && !variable.startsWith("new")
                    && !variable.startsWith("count(") && !variable.contains(",");

            String replacement = useVariable ? SIMPLE_COUNT_VALUE : COMPLEX_COUNT_VALUE;
            countQuery = matcher.replaceFirst(String.format(COUNT_REPLACEMENT_TEMPLATE, replacement));
        } else {
            countQuery = matcher.replaceFirst(String.format(COUNT_REPLACEMENT_TEMPLATE, countProjection));
        }

        return countQuery.replaceFirst(ORDER_BY_PART, "");
    }

    /**
     * Returns whether the given query contains named parameters.
     *
     * @param query can be {@literal null} or empty.
     * @return
     */
    public static boolean hasNamedParameter(String query) {
        return StringUtils.hasText(query) && NAMED_PARAMETER.matcher(query).find();
    }

    /**
     * Returns whether the given JPQL query contains a constructor expression.
     *
     * @param query must not be {@literal null} or empty.
     * @return
     * @since 1.10
     */
    public static boolean hasConstructorExpression(String query) {

        Assert.hasText(query, "Query must not be null or empty!");

        return CONSTRUCTOR_EXPRESSION.matcher(query).find();
    }

    /**
     * Returns the projection part of the query, i.e. everything between {@code select} and {@code from}.
     *
     * @param query must not be {@literal null} or empty.
     * @return
     * @since 1.10.2
     */
    public static String getProjection(String query) {

        Assert.hasText(query, "Query must not be null or empty!");

        Matcher matcher = PROJECTION_CLAUSE.matcher(query);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * Check any given order for presence of at least one property offending the
     * {@link #PUNCTATION_PATTERN} and throw an {@link Exception} indicating potential unsafe order by expression.
     *
     * @param order
     */
    private static void checkSortExpression(Order order) {
        if (PUNCTATION_PATTERN.matcher(order.getProperty()).find()) {
            throw new InvalidDataAccessApiUsageException(String.format(UNSAFE_PROPERTY_REFERENCE, order));
        }
    }

    //region apply query

    public static <T> String applyQuery(Class<T> domain, QueryInfo query) {
        return applyQuery(domain, query, null);
    }

    /**
     * Adds {@literal order by} clause to the JPQL query. Uses the DEFAULT_ALIAS to bind the sorting property to.
     *
     * @param query
     * @return
     */
    public static <T> String applyQuery(Class<T> domain, QueryInfo query, String alias) {
        String sql = query.getSql();
        sql = Sql2oCrudService.instance().buildFilterSql(domain, sql, query.getQueryItems());
        String orderNull = " order by null";
        if (query.getSort() != null) {
            TableInfo ti = PojoData.forClass(domain).getTableInfo();
            List<Sort.Order> orders = new ArrayList<>();
            for (Order order : query.getSort()) {
                PocoColumn column = ti.getColumn(order.getProperty());
                if (column != null) {
                    orders.add(order.withProperty(column.ColumnName));
                }
            }
            if (orders.size() > 0) {
                sql = applySorting(sql, new Sort(orders), alias);
            } else {
                sql += orderNull;
            }
        } else {
            sql += orderNull;
        }
        return sql;
    }

    //endregion
}
