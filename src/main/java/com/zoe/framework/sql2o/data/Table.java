package com.zoe.framework.sql2o.data;

import com.zoe.framework.sql2o.quirks.Quirks;
import com.zoe.framework.sql2o.tools.CamelCaseUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * Represents an offline result set with columns and rows and data.
 */
public class Table implements Serializable {
    private String schema;
    private String name;
    private List<Row> rows;
    private List<Column> columns;
    private List<Column> primaryKey;
    private final boolean isCaseSensitive;
    private final Quirks quirks;
    private final Map<String, Integer> columnNameToIdxMap;

    public Table(boolean isCaseSensitive, Quirks quirks) {
        this(isCaseSensitive, quirks, new ArrayList<>());
    }

    public Table(boolean isCaseSensitive, Quirks quirks,
                 List<Column> columns) {
        this.isCaseSensitive = isCaseSensitive;
        this.quirks = quirks;

        this.name = name == null ? "" : name;
        this.rows = new ArrayList<>();
        this.columns = columns;

        this.columnNameToIdxMap = new HashMap<>();
        int i = 0;
        for (Column column : columns) {
            columnNameToIdxMap.put(column.getName(), i++);
        }
    }

    public Table(Map<String, Integer> columnNameToIdxMap,
                 boolean isCaseSensitive, Quirks quirks,
                 List<Column> columns) {
        this.columnNameToIdxMap = columnNameToIdxMap;
        this.isCaseSensitive = isCaseSensitive;
        this.quirks = quirks;

        this.name = name == null ? "" : name;
        this.rows = new ArrayList<>();
        this.columns = columns;
        this.primaryKey = new ArrayList<>();
        for (Column column : columns) {
            if (column.getIsPrimaryKey()) {
                this.primaryKey.add(column);
            }
        }
    }

    public Map<String, Integer> getColumnNameToIdxMap() {
        return columnNameToIdxMap;
    }

    public Quirks getQuirks() {
        return quirks;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public Table setName(String name) {
        this.name = name;
        return this;
    }

    public List<Row> rows() {
        return rows;
    }

    public void addRow(Row row) {
        rows.add(row);
    }

    public Row newRow() {
        Row row = new Row(this);
        row.setRowState(RowState.Added);
        return row;
    }

    public Column addColumn(String columnName) {
        return addColumn(columnName, String.class);
    }

    public Column addColumn(String columnName, Class<?> clazz) {
        //字段类型考虑在quirks中增加根据java类型转换为数据库类型的方法
        String type = "VARCHAR2";
        if (String.class.isAssignableFrom(clazz)) {
            type = "VARCHAR2";
        } else if (Date.class.isAssignableFrom(clazz)) {
            type = "DATE";
        } else if (Integer.class.isAssignableFrom(clazz)
                || Long.class.isAssignableFrom(clazz)
                || Double.class.isAssignableFrom(clazz)
                || BigDecimal.class.isAssignableFrom(clazz)) {
            type = "NUMBER";
        }
        Column column = new Column(columnName, columns().size(), type);
        columns.add(column);
        return column;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Column> columns() {
        return columns;
    }

    public List<Column> getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(List<Column> primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Row getRow(int rowIndex) {
        Row row = rows.get(rowIndex);
        if (!row.getRowState().equals(RowState.Added)) {
            row.setRowState(RowState.Modified);
        }
        return row;
    }

    public Column getColumn(int columnIndex) {
        return columns.get(columnIndex);
    }

    public Column getColumn(String columnName) {
        for (Column column : columns) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        return null;
    }

    public Column removeColumn(int columnIndex) {
        Column column = columns.remove(columnIndex);
        if (column != null) {
            columnNameToIdxMap.remove(CamelCaseUtils.underscoreToCamelCase(column.getName()));
        }
        return column;
    }

    public Column removeColumn(String columnName) {
        Column columnToRemove = null;
        for (Column column : columns) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                columnToRemove = column;
            }
        }
        if (columnToRemove != null) {
            columns.remove(columnToRemove);
            columnNameToIdxMap.remove(CamelCaseUtils.underscoreToCamelCase(columnToRemove.getName()));
        }
        return columnToRemove;
    }

    public List<Map<String, Object>> asList() {
        /*return new AbstractList<Map<String, Object>>() {
            @Override
            public Map<String, Object> get(int index) {
                return rows.get(index).asMap();
            }

            @Override
            public int size() {
                return rows.size();
            }
        };*/
        List<Map<String, Object>> list = new ArrayList<>();
        for (Row row : rows) {
            list.add(row.asMap2());
        }
        return list;
    }

    public Map<String, Object> toSchema(boolean autoDeriveColumnNames) {
        Map<String, Object> map = new HashMap<>();
        if(autoDeriveColumnNames) {
            for (String key : columnNameToIdxMap.keySet()) {
                map.put(key, null);
            }
        } else {
            for (Column column : columns) {
                map.put(column.getName(), null);
            }
        }
        return map;
    }

    public List<Row> select(String columnName, Object columnValue) {
        List<Row> list = new ArrayList<>();
        if (columnValue != null) {
            for (Row row : rows) {
                if (Objects.equals(row.getString(columnName), columnValue.toString())) {
                    list.add(row);
                }
            }
        }
        return list;
    }
}
