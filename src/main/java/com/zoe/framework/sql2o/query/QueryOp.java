package com.zoe.framework.sql2o.query;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * QueryOp
 * Created by caizhicong on 2017/7/14.
 */
public enum  QueryOp {
    /**
     * Apply a "between" constraint to the named property
     */
    between(0),
    /**
     * Apply a "not between" constraint to the named property
     */
    notBetween(1),
    /**
     * Apply an "equal" constraint to the named property
     */
    eq(2),
    /**
     * Apply an "equal" constraint to the named property.
     */
    eqOrIsNull(3),
    /**
     * Apply an "equal" constraint to two properties
     */
    eqProperty(4),
    /**
     * Apply a "greater than or equal" constraint to the named property
     */
    ge(5),
    /**
     * Apply a "greater than or equal" constraint to two properties
     */
    geProperty(6),
    /**
     * Apply a "greater than" constraint to the named property
     */
    gt(7),
    /**
     * Apply a "greater than" constraint to two properties
     */
    gtProperty(8),
    /**
     * Apply an "in" constraint to the named property
     */
    in(9),
    /**
     * Apply an "not in" constraint to the named property
     */
    notIn(10),
    /**
     * Apply an "is not null" constraint to the named property
     */
    isNotNull(11),
    /**
     * Apply an "is null" constraint to the named property
     */
    isNull(12),
    /**
     * Apply a "less than or equal" constraint to the named property
     */
    le(13),
    /**
     * Apply a "less than or equal" constraint to two properties
     */
    leProperty(14),
    /**
     * Apply a "like" constraint to the named property
     */
    like(15),
    /**
     * Apply a "left like" constraint to the named property
     */
    llike(16),
    /**
     * Apply a "right like" constraint to the named property
     */
    rlike(17),
    /**
     * Apply a "less than" constraint to the named property
     */
    lt(18),
    /**
     * Apply a "less than" constraint to two properties
     */
    ltProperty(19),
    /**
     * Apply a "not equal" constraint to the named property
     */
    ne(20),
    /**
     * Apply a "not equal" constraint to the named property.
     */
    neOrIsNotNull(21),
    /**
     * Apply a "not equal" constraint to two properties
     */
    neProperty(22);

    private int op;

    /**
     * public static final byte eq = 0;
     * public static final byte neq = 1;
     * public static final byte gt = 2;
     * public static final byte lt = 3;
     * public static final byte ge = 4;
     * public static final byte le = 5;
     * public static final byte like = 6;
     * public static final byte rlike = 7;
     * public static final byte llike = 8;
     * public static final byte in = 9;
     * public static final byte notin = 10;
     * public static final byte between = 11;
     * public static final byte nvl = 12;
     * public static final byte notnvl = 13;
     */

    QueryOp(int op) {
        this.op = op;
    }

    public int getOp() {
        return this.op;
    }

    public boolean aboutNull() {
        return op == isNull.op || op == isNotNull.op;
    }

    public boolean aboutLike() {
        return op == like.op || op == llike.op || op == rlike.op;
    }

    public String format(String field, String paramName, Object value, Class<?> type) {
        String opStr;
        String inValue;
        switch (this) {
            case like:
            case llike:
            case rlike:
                opStr = "like";
                break;
            case ne:
                opStr = "!=";
                break;
            case gt:
                opStr = ">";
                break;
            case lt:
                opStr = "<";
                break;
            case isNull:
                return String.format("%s is null", field);
            case isNotNull:
                return String.format("%s is not null", field);
            case eqOrIsNull:
                return String.format("( %s = :%s or %s is null )", field, paramName, field);
            case neOrIsNotNull:
                return String.format("( %s != :%s or %s is not null )", field, paramName, field);
            case in:
                inValue = getInValue(value, type);
                return String.format("%s in (%s)", field, inValue);
            case notIn:
                inValue = getInValue(value, type);
                return String.format("%s not in (%s)", field, inValue);
            default:
                opStr = "=";
                break;
        }
        return String.format("%s %s :%s", field, opStr, paramName);
    }

    public Object formatValue(Object value) {
        if(value == null) return null;
        switch (this) {
            case like:
                return "%" + value + "%";
            case llike:
                return "%" + value;
            case rlike:
                return value + "%";
        }
        return value;
    }

    private String getInValue(Object value, Class<?> type){
        String valueIn = null;
        List list = null;
        if(value instanceof String){
            list = Arrays.asList(((String) value).split(","));
        }
        else if (value instanceof List) {
            list = (List) value;
        }
        if (list != null && list.size() > 0) {
            Class elClass = list.get(0).getClass();
            if (Number.class.isAssignableFrom(elClass)) {
                valueIn = StringUtils.join(list.toArray(), ",");
            } else if (String.class.isAssignableFrom(elClass)) {
                //todo sql-filter
                String[] strings = (String[])list.toArray(new String[0]);
                for (int i = 0; i < strings.length; i++) {
                    strings[i] = filter(strings[i], true);
                }
                valueIn = "'" + StringUtils.join(strings, "','") + "'";
            }
        }
        return valueIn;
    }

    public static String filter(String input, boolean setNullIfFound) {
        if (StringUtils.isBlank(input))
            return null;
        Matcher matcher = sqlPattern.matcher(input);
        if (matcher.find()) {
            if (setNullIfFound) {
                return null;
            }
            input = matcher.replaceAll("");
        }
        return input;
    }

   static final Pattern sqlPattern = Pattern.compile("(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)",Pattern.CASE_INSENSITIVE);
}
