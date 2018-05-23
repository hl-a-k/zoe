package com.zoe.framework.sql2o.data;

import com.zoe.framework.sql2o.ResultSetIteratorBase;
import com.zoe.framework.sql2o.Sql2oException;
import com.zoe.framework.sql2o.quirks.Quirks;
import com.zoe.framework.sql2o.tools.CamelCaseUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aldenquimby@gmail.com
 */
public class TableResultSetIterator extends ResultSetIteratorBase<Row> {
    private Map<String, Integer> columnNameToIdxMap;
    private List<Column> columns;
    private Table table;

    public TableResultSetIterator(ResultSet rs, boolean isCaseSensitive,
                                  Boolean isAutoDeriveColumnNames, Quirks quirks, LazyTable lt) {
        super(rs, isCaseSensitive, quirks);

        this.columnNameToIdxMap = new HashMap<String, Integer>();
        this.columns = new ArrayList<Column>();

        try {
            lt.setName(meta.getTableName(1));//ojdbc6 并没有实现这个方法。。。

            for (int colIdx = 1; colIdx <= meta.getColumnCount(); colIdx++) {
                String colName = getColumnName(colIdx);
                String colType = meta.getColumnTypeName(colIdx);
                int sqlType = meta.getColumnType(colIdx);
                Column column = new Column(colName, colIdx - 1, colType);
                column.setSqlType(sqlType);
                columns.add(column);

                // TODO support autoDeriveColumnNames
                if (isAutoDeriveColumnNames) {
                    String colMapName = CamelCaseUtils
                            .underscoreToCamelCase(colName);
                    if (!isCaseSensitive)
                        colMapName = colMapName.toLowerCase();
                    //TODO 已存在的字段是否覆盖的问题！！！现在是后面的同名字段会覆盖前面！
                    //if (columnNameToIdxMap.containsKey(colMapName)) continue;
                    columnNameToIdxMap.put(colMapName, colIdx - 1);
                } else {
                    String colMapName = isCaseSensitive ? colName : colName
                            .toLowerCase();
                    //TODO 已存在的字段是否覆盖的问题！！！现在是后面的同名字段会覆盖前面！
                    //if (columnNameToIdxMap.containsKey(colMapName)) continue;
                    columnNameToIdxMap.put(colMapName, colIdx - 1);
                }
            }
        } catch (SQLException e) {
            throw new Sql2oException(
                    "Error while reading metadata from database", e);
        }

        lt.setColumns(columns);

        table = new Table(columnNameToIdxMap, isCaseSensitive, quirks, columns);
    }

    @Override
    protected Row readNext() throws SQLException {
        Row row = new Row(table);
        for (Column column : columns) {
            row.addValue(column.getIndex(),
                    quirks.getRSVal(rs, column.getIndex() + 1));
        }
        return row;
    }

    public Table newTable() {
        return table;
    }
}
