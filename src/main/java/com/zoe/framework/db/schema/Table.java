package com.zoe.framework.db.schema;

import com.zoe.framework.db.schema.utils.CamelCaseUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caizhicong on 2016/3/21.
 */
public class Table implements IDBObject {

    public Table() {
        setColumns(new ArrayList<>());
    }

    public Table(String name) {
        this(null, name, TableType.Table, null);
    }

    public Table(String schema, String name) {
        this(schema, name, TableType.Table, null);
    }

    public Table(String schema, String name, TableType tableType, String classname) {
        setName(name);
        setSchemaName(schema);
        setTableType(tableType);
        setClassName(classname);
        setColumns(new ArrayList<>());
    }

    public List<Column> getPrimaryKeys() {
        List<Column> columns = new ArrayList<>();
        for (Column column : getColumns()) {
            if (column.isPrimaryKey()) {
                columns.add(column);
            }
        }
        return columns;
    }

    public boolean getHasPrimaryKey() {
        return getPrimaryKey() != null && getPrimaryKey().getColumns().size() > 0;
    }

    private PrimaryKey primaryKey;

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey value) {
        primaryKey = value;
    }

    private String schemaName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String value) {
        schemaName = value;
    }

    public String getOwner() {
        return getSchemaName();
    }

    public void setOwner(String value) {
        setSchemaName(value);
    }

    private String comment;

    public String getComment() {
        if (comment != null) {
            return comment;
        }
        return getName();
    }

    public void setComment(String value) {
        comment = value;
    }

    private String name;

    public String getName() {
        return name;
    }

    public String getName(boolean withSchema) {
        if (withSchema && !StringUtils.isBlank(schemaName)) {
            return schemaName + "." + name;
        }
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    private String className;

    public String getClassName() {
        if (className != null) {
            return className;
        }
        return CamelCaseUtils.toPascalCase(getName());
    }

    public void setClassName(String value) {
        className = value;
    }

    private List<Column> columns;

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> value) {
        columns = value;
    }

    public Column getColumn(String ColumnName) {
        Column result = null;
        for (Column col : getColumns()) {
            if (col.getName().equalsIgnoreCase(ColumnName)) {
                result = col;
                break;
            }
        }
        return result;
    }

    public Column getColumnByPropertyName(String PropertyName) {
        Column result = null;
        for (Column col : getColumns()) {
            if (col.getPropertyName().equalsIgnoreCase(PropertyName)) {
                result = col;
                break;
            }
        }
        return result;
    }

    private TableType tableType;

    public TableType getTableType() {
        return tableType;
    }

    public void setTableType(TableType value) {
        tableType = value;
    }

    @Override
    public String toString() {
        return getName();
    }
}
