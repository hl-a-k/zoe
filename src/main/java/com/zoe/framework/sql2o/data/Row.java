package com.zoe.framework.sql2o.data;

import static java.util.Arrays.asList;
import static com.zoe.framework.sql2o.converters.Convert.throwIfNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import com.zoe.framework.sql2o.Sql2oException;
import com.zoe.framework.sql2o.converters.ConverterException;
import com.zoe.framework.sql2o.util.CastUtils;

/**
 * Represents a result set row.
 */
public class Row implements Serializable {

    private Object[] values;
    private Boolean[] changes;
    private Table table;
    private RowState rowState = RowState.Unchanged;

    public Row(Table table) {
        // lol. array works better
        this.table = table;
        int columnCnt = table.columns().size();
        this.values = new Object[columnCnt];
        this.changes = new Boolean[columnCnt];
    }

    void addValue(int columnIndex, Object value) {
        values[columnIndex] = value;
    }

    public Object getObject(int columnIndex) {
        return values[columnIndex];
    }

    public Object getObject(String columnName) {
        Integer index = table.getColumnNameToIdxMap().get(table.isCaseSensitive() ? columnName
                : columnName.toLowerCase());
        if (index != null)
            return getObject(index);
        throw new Sql2oException(String.format(
                "Column with name '%s' does not exist", columnName));
    }

    public Boolean getChange(int columnIndex) {
        Boolean change = changes[columnIndex];
        if (change != null) {
            return change;
        }
        return false;
    }

    public Boolean getChange(String columnName) {
        Integer index = table.getColumnNameToIdxMap().get(table.isCaseSensitive() ? columnName
                : columnName.toLowerCase());
        if (index != null)
            return getChange(index);
        throw new Sql2oException(String.format(
                "Column with name '%s' does not exist", columnName));
    }

    public <V> V getObject(int columnIndex, Class<V> clazz) {
        try {
            return (V) throwIfNull(clazz, table.getQuirks().converterOf(clazz)).convert(
                    getObject(columnIndex));
        } catch (ConverterException ex) {
            throw new Sql2oException("Error converting value", ex);
        }
    }

    public <V> V getObject(String columnName, Class<V> clazz) {
        try {
            return (V) throwIfNull(clazz, table.getQuirks().converterOf(clazz)).convert(
                    getObject(columnName));
        } catch (ConverterException ex) {
            throw new Sql2oException("Error converting value", ex);
        }
    }

    public BigDecimal getBigDecimal(int columnIndex) {
        return this.getObject(columnIndex, BigDecimal.class);
    }

    public BigDecimal getBigDecimal(String columnName) {
        return this.getObject(columnName, BigDecimal.class);
    }

    public Boolean getBoolean(int columnIndex) {
        return this.getObject(columnIndex, Boolean.class);
    }

    public Boolean getBoolean(String columnName) {
        return this.getObject(columnName, Boolean.class);
    }

    public Double getDouble(int columnIndex) {
        return this.getObject(columnIndex, Double.class);
    }

    public Double getDouble(String columnName) {
        return this.getObject(columnName, Double.class);
    }

    public Float getFloat(int columnIndex) {
        return this.getObject(columnIndex, Float.class);
    }

    public Float getFloat(String columnName) {
        return this.getObject(columnName, Float.class);
    }

    public Long getLong(int columnIndex) {
        return this.getObject(columnIndex, Long.class);
    }

    public Long getLong(String columnName) {
        return this.getObject(columnName, Long.class);
    }

    public Integer getInteger(int columnIndex) {
        return this.getObject(columnIndex, Integer.class);
    }

    public Integer getInteger(String columnName) {
        return this.getObject(columnName, Integer.class);
    }

    public Short getShort(int columnIndex) {
        return this.getObject(columnIndex, Short.class);
    }

    public Short getShort(String columnName) {
        return this.getObject(columnName, Short.class);
    }

    public Byte getByte(int columnIndex) {
        return this.getObject(columnIndex, Byte.class);
    }

    public Byte getByte(String columnName) {
        return this.getObject(columnName, Byte.class);
    }

    public Date getDate(int columnIndex) {
        return this.getObject(columnIndex, Date.class);
    }

    public Date getDate(String columnName) {
        return this.getObject(columnName, Date.class);
    }

    public String getString(int columnIndex) {
        return this.getObject(columnIndex, String.class);
    }

    public String getString(String columnName) {
        return this.getObject(columnName, String.class);
    }

    public String getString(String columnName, String defaultValue) {
        String value = this.getObject(columnName, String.class);
        return value != null ? value : defaultValue;
    }

    /**
     * View row as a simple map.
     */
    public Map<String, Object> asMap() {
        final List<Object> listOfValues = asList(values);
        return new Map<String, Object>() {
            public int size() {
                return values.length;
            }

            public boolean isEmpty() {
                return size() == 0;
            }

            public boolean containsKey(Object key) {
                return table.getColumnNameToIdxMap().containsKey(key);
            }

            public boolean containsValue(Object value) {
                return listOfValues.contains(value);
            }

            public Object get(Object key) {
                Integer index = table.getColumnNameToIdxMap().get(key);
                return index != null ? values[index] : null;
            }

            public Object put(String key, Object value) {
                throw new UnsupportedOperationException("Row map is immutable.");
            }

            public Object remove(Object key) {
                throw new UnsupportedOperationException("Row map is immutable.");
            }

            public void putAll(Map<? extends String, ?> m) {
                throw new UnsupportedOperationException("Row map is immutable.");
            }

            public void clear() {
                throw new UnsupportedOperationException("Row map is immutable.");
            }

            public Set<String> keySet() {
                return table.getColumnNameToIdxMap().keySet();
            }

            public Collection<Object> values() {
                return listOfValues;
            }

            public Set<Entry<String, Object>> entrySet() {
                Set<Entry<String, Object>> entries = new java.util.HashSet<>();
                for (String key : table.getColumnNameToIdxMap().keySet()) {
                    final String aKey = key;
                    final Object aVal = values[table.getColumnNameToIdxMap().get(key)];
                    entries.add(new Map.Entry<String, Object>() {
                        @Override
                        public String getKey() {
                            return aKey;
                        }

                        @Override
                        public Object getValue() {
                            // TODO Auto-generated method stub
                            return aVal;
                        }

                        @Override
                        public Object setValue(Object value) {
                            // TODO Auto-generated method stub
                            return null;
                        }
                    });
                }
                return entries;
                /*
                 * throw new UnsupportedOperationException(
				 * "Row map does not support entrySet.");
				 */
            }
        };
    }

    /**
     * View row as a simple map.
     */
    public Map<String, Object> asMap2() {
        Map<String, Object> map = new HashMap<>();
        for (String key : table.getColumnNameToIdxMap().keySet()) {
            Object value = values[table.getColumnNameToIdxMap().get(key)];
            map.put(key, value);
        }
        return map;
    }

    public void setObject(String columnName, Object value) {
        Integer index = table.getColumnNameToIdxMap().get(table.isCaseSensitive() ? columnName
                : columnName.toLowerCase());
        if (index != null) {
            Column column = table.getColumn(index);
            if (column != null) {
                value = CastUtils.cast(value, column.getJavaType(), null);
            }
            values[index] = value;
            changes[index] = true;
        }
    }

    public Table getTable() {
        return table;
    }

    public RowState getRowState() {
        return rowState;
    }

    public void setRowState(RowState rowState) {
        this.rowState = rowState;
    }
}
