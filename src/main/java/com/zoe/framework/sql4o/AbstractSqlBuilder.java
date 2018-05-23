package com.zoe.framework.sql4o;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Abstract base class for builders. Contains helper methods.
 *
 * @author John Krasnay <john@krasnay.ca>
 */
public abstract class AbstractSqlBuilder {

    private static final String NAME_REGEX = "[a-z][_a-z0-9]*";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX, Pattern.CASE_INSENSITIVE);
    protected Map<String, Object> args = new LinkedHashMap<>();
    private int paramIndex = 0;

    public AbstractSqlBuilder() {

    }

    /**
     * Copy constructor. Used by cloneable creators.
     *
     * @param other AbstractSqlCreator being cloned.
     */
    public AbstractSqlBuilder(AbstractSqlBuilder other) {
        this.paramIndex = other.paramIndex;
    }

    /**
     * Sets a parameter for the creator.
     */
    public AbstractSqlBuilder setParameter(String name, Object value) {
        args.put(name, value);
        return this;
    }

    /**
     * Sets a parameter for the creator.
     * it will check the name specification
     */
    public AbstractSqlBuilder setParameterWithCheck(String name, Object value) {
        if (NAME_PATTERN.matcher(name).matches()) {
            args.put(name, value);
        } else {
            throw new IllegalArgumentException(
                    "'" + name + "' is not a valid parameter name. Names must start with a letter, and contain only letters, numbers, and underscores.");
        }
        return this;
    }


    /**
     * Allocate and return a new parameter that is unique within this
     * SelectCreator. The parameter is of the form "paramN", where N is an
     * integer that is incremented each time this method is called.
     */
    public String allocateParameter() {
        return "param" + paramIndex++;
    }

    public String allocateParameter(String param) {
        return param + paramIndex++;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public Map<String, Object> getParameterMap() {
        return args;
    }

    /**
     * Constructs a list of items with given separators.
     *
     * @param sql  StringBuilder to which the constructed string will be
     *             appended.
     * @param list List of objects (usually strings) to join.
     * @param init String to be added to the start of the list, before any of the
     *             items.
     * @param sep  Separator string to be added between items in the list.
     */
    protected void appendList(StringBuilder sql, List<?> list, String init, String sep) {

        boolean first = true;

        for (Object s : list) {
            if (first) {
                sql.append(init);
            } else {
                sql.append(sep);
            }
            sql.append(s);
            first = false;
        }
    }

    protected String appendAlias(String alias) {
        return (!StringUtils.isBlank(alias) ? " " + alias + " " : "");
    }
}
