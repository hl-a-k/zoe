package com.zoe.framework.sql2o.data;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.*;
import javax.persistence.Column;
import javax.persistence.Table;

import com.zoe.framework.sql2o.Sql2oPropertyConfigurer;
import com.zoe.framework.sql2o.reflection.PojoIntrospector;
import com.zoe.framework.sql2o.reflection.PojoProperty;
import com.zoe.framework.sql2o.tools.CamelCaseUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;

public class TableInfo {
    
    private String schema;

    public final String getSchema() {
        return schema;
    }

    public final void setSchema(String value) {
        schema = value;
    }

    private String name;

    public final String getName() {
        return name;
    }

    public final void setName(String value) {
        name = value;
    }

    /**
     * The database table name
     */
    private String tableName;

    public final String getTableName() {
        if (tableName != null) {
            return tableName;
        }
        if (!StringUtils.isBlank(schema)) {
            tableName = schema + "." + name;
        } else {
            tableName = name;
        }
        return tableName;
    }

    /**
     * The name of the primary key column of the table
     */
    private List<String> primaryKeys;

    public final List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public final void setPrimaryKeys(List<String> value) {
        primaryKeys = value;
    }

    /**
     * 标志该表是否为复合主键
     */
    private boolean compositePrimaryKey;

    public final boolean getCompositePrimaryKey() {
        return compositePrimaryKey;
    }

    private void setCompositePrimaryKey(boolean value) {
        compositePrimaryKey = value;
    }

    private String primaryKey;

    public final String getPrimaryKey() {
        return primaryKey;
    }

    public final void setPrimaryKey(String value) {
        primaryKey = value;
    }

    /**
     * True if the primary key column is an auto-incrementing
     */
    private boolean autoIncrement;

    public final boolean getAutoIncrement() {
        return autoIncrement;
    }

    public final void setAutoIncrement(boolean value) {
        autoIncrement = value;
    }

    /**
     * The name of the sequence used for auto-incrementing Oracle primary key
     * fields
     */
    private String sequenceName;

    public final String getSequenceName() {
        return sequenceName;
    }

    public final void setSequenceName(String value) {
        sequenceName = value;
    }

    /**
     * The column name which use sequence to auto-increment
     */
    private String sequenceColumn;

    public final String getSequenceColumn() {
        return sequenceColumn;
    }

    public final void setSequenceColumn(String value) {
        sequenceColumn = value;
    }

    private LinkedCaseInsensitiveMap<PocoColumn> columns;

    /**
     * 获取表字段名称列表
     *
     * @return
     */
    public LinkedCaseInsensitiveMap<PocoColumn> getColumns() {
        return columns;
    }

    private LinkedCaseInsensitiveMap<PocoColumn> properties;

    /**
     * 获取实体属性名称字典
     *
     * @return
     */
    public LinkedCaseInsensitiveMap<PocoColumn> getProperties() {
        return properties;
    }

    private LinkedCaseInsensitiveMap<PocoColumn> unUpdatableColumns;

    /**
     * 获取不可更新的表字段名称列表
     *
     * @return
     */
    public LinkedCaseInsensitiveMap<PocoColumn> getUnUpdatableColumns() {
        return unUpdatableColumns;
    }


    /**
     * 根据实体属性名称获取对应数据库表字段名称
     *
     * @param propertyName 实体属性名称
     * @return 数据库表字段名称
     */
    public PocoColumn getColumn(String propertyName) {
        PocoColumn pocoColumn = properties.get(propertyName);
        if (pocoColumn == null) {
            pocoColumn = columns.get(propertyName);
        }
        return pocoColumn;
    }

    /**
     * 根据实体属性名称获取对应数据库表字段名称
     *
     * @param propertyName 实体属性名称
     * @return 数据库表字段名称
     */
    public String getColumnName(String propertyName) {
        PocoColumn column = getColumn(propertyName);
        if (column != null) {
            return column.ColumnName;
        }
        return propertyName;
    }

    /**
     * 根据数据库表字段名称获取对应实体属性名称
     *
     * @param columnName 数据库表字段名称
     * @return 实体属性名称
     */
    public String getPropertyName(String columnName) {
        PocoColumn column = getColumns().get(columnName);
        if (column != null) {
            return column.PropertyInfo.name;
        }
        return columnName;
    }

    /**
     * Creates and populates a TableInfo from the attributes of a POCO
     *
     * @param t The POCO type
     * @return A TableInfo instance
     */
    public static TableInfo fromPoco(java.lang.Class t) {
        TableInfo ti = new TableInfo();

        ti.columns = new LinkedCaseInsensitiveMap<>();
        ti.properties = new LinkedCaseInsensitiveMap<>();
        ti.unUpdatableColumns = new LinkedCaseInsensitiveMap<>();
        ti.primaryKeys = new ArrayList<>();

        // Get the table name
        Table a = (Table) t.getAnnotation(Table.class);
        ti.setName(a == null ? t.getSimpleName() : a.name());
        ti.setSchema(a == null ? null : a.schema());

        //todo 变更表名主键信息
        String tableName = Sql2oPropertyConfigurer.getProperty(ti.getName()),
                schemaName = null;
        Map<String,String> columnNamesMap = null;
        boolean hasCustomConfig = !StringUtils.isBlank(tableName);
        boolean hasColumnsMap = false;
        if(hasCustomConfig) {
            if(tableName.contains(".")){
                String[] arr = StringUtils.split(tableName,".");
                schemaName = arr[0];
                tableName = arr[1];
            }
            if (!StringUtils.equalsIgnoreCase(ti.getName(), tableName)) {
                ti.setName(tableName);
            }
            if (!StringUtils.isBlank(schemaName) && !StringUtils.equalsIgnoreCase(ti.getSchema(), schemaName)) {
                ti.setSchema(schemaName);
            }
            columnNamesMap = Sql2oPropertyConfigurer.getByPrefix(ti.getName());
            hasColumnsMap = columnNamesMap.size() > 0;
        }

        Map<String, PojoProperty> propertyMap = PojoIntrospector.collectProperties(t);
        for (PojoProperty property : propertyMap.values()) {
            PocoColumn pc = new PocoColumn();
            Field field = property.getField();
            Column column = field.getAnnotation(Column.class);
            if(column == null && property.getSetMethod() != null) {
                column = property.getSetMethod().getAnnotation(Column.class);
                pc.hasColumnAnnotation = false;
            }
            String columnName;
            if (column == null || column.name().isEmpty()) {
                columnName = CamelCaseUtils.camelCaseToUnderscore(property.name);
            } else {
                pc.nullable(column.nullable());
                pc.insertable(column.insertable());
                pc.updatable(column.updatable());
                columnName = column.name();
            }
            if(Date.class.isAssignableFrom(field.getType())){
                pc.IsDateTime = true;
            }

            //映射字段（允许实际表字段与系统预设的字段名称不一致）
            String columnNameMap;
            if (hasColumnsMap && !StringUtils.isBlank(columnNameMap = columnNamesMap.get(columnName))
                    && !StringUtils.equalsIgnoreCase(columnName, columnNameMap)) {
                columnName = columnNameMap;
            }
            //set columnName
            property.columnName(columnName);

            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                pc.IsPrimaryKey = true;
                //pc.updatable(false);
                ti.setPrimaryKey(columnName);
                ti.getPrimaryKeys().add(columnName);

                SequenceGenerator seq = field.getAnnotation(SequenceGenerator.class);
                if (seq != null) {
                    ti.setSequenceName(seq.sequenceName());
                    ti.setSequenceColumn(columnName);
                }

                GeneratedValue gValue = field.getAnnotation(GeneratedValue.class);
                if (gValue != null) {
                    if (gValue.strategy() == GenerationType.IDENTITY) {
                        pc.insertable(false);
                        ti.setAutoIncrement(true);
                    }
                }
            }

            pc.PropertyInfo = property;
            pc.ColumnName = columnName;

            // Store it
            ti.columns.put(columnName, pc);
            ti.properties.put(property.name, pc);
            if(!pc.updatable()){
                ti.unUpdatableColumns.put(columnName, pc);
            }
        }

        // Set If Composite primaryKey
        ti.setCompositePrimaryKey(ti.getPrimaryKeys().size() > 1);

        return ti;
    }
}
