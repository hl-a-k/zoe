package com.zoe.framework.db.schema.reader;

import com.zoe.framework.db.schema.query.QueryHelper;
import com.zoe.framework.db.schema.reader.MysqlSchemaReader;

import java.util.List;

/**
 * Created by caizhicong on 2016/5/16.
 */
public class MysqlInnoDBSchemaReader extends MysqlSchemaReader {
    private static final String ALL_TABLE_COLUMNS_SQL = "SELECT" + "\r\n" +
            "      TABLE_SCHEMA as `Database`," + "\r\n" +
            "      TABLE_NAME as TableName," + "\r\n" +
            "      COLUMN_NAME as ColumnName," + "\r\n" +
            "      ORDINAL_POSITION as OrdinalPosition," + "\r\n" +
            "      COLUMN_DEFAULT as DefaultSetting," + "\r\n" +
            "      IS_NULLABLE as IsNullable," + "\r\n" +
            "      DATA_TYPE as DataType," + "\r\n" +
            "      CHARACTER_MAXIMUM_LENGTH as Length," + "\r\n" +
            "      IF(EXTRA = 'auto_increment', 1, 0) as IsIdentity" + "\r\n" +
            "FROM" + "\r\n" +
            "      INFORMATION_SCHEMA.COLUMNS" + "\r\n" +
            "WHERE" + "\r\n" +
            "      TABLE_SCHEMA = ?DatabaseName" + "\r\n" +
            "ORDER BY" + "\r\n" +
            "      OrdinalPosition ASC";

    private static final String ALL_TABLE_FOREIGN_TABLES = "SELECT" + "\r\n" +
            "      table_name as FK_TABLE," + "\r\n" +
            "      referenced_column_name as PK_COLUMN," + "\r\n" +
            "      referenced_table_name as PK_TABLE," + "\r\n" +
            "      column_name as FK_Column," + "\r\n" +
            "      constraint_name as CONSTRAINT_NAME" + "\r\n" +
            "\r\n" +
            "FROM" + "\r\n" +
            "      INFORMATION_SCHEMA.KEY_COLUMN_USAGE" + "\r\n" +
            "WHERE" + "\r\n" +
            "      TABLE_SCHEMA = ?DatabaseName" + "\r\n" +
            "      AND REFERENCED_TABLE_NAME IS NOT NULL";

    private static final String ALL_TABLE_INDEXES_SQL = "SELECT" + "\r\n" +
            "      tc.table_name as TableName," + "\r\n" +
            "      tc.table_schema as Owner," + "\r\n" +
            "      kc.column_name as ColumnName," + "\r\n" +
            "      tc.constraint_type as ConstraintType," + "\r\n" +
            "      tc.constraint_name as ConstraintName" + "\r\n" +
            "FROM" + "\r\n" +
            "      INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc" + "\r\n" +
            "      INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE kc" + "\r\n" +
            "            ON tc.table_schema = kc.table_schema" + "\r\n" +
            "            AND tc.table_name = kc.table_name" + "\r\n" +
            "            AND tc.constraint_name = kc.constraint_name" + "\r\n" +
            "WHERE" + "\r\n" +
            "       tc.table_schema = ?DatabaseName";

    private static final String ALL_TABLE_PRIMARY_TABLES = "SELECT" + "\r\n" +
            "      referenced_table_name as PK_TABLE," + "\r\n" +
            "      table_name as FK_TABLE," + "\r\n" +
            "      column_name as FK_COLUMN" + "\r\n" +
            "FROM" + "\r\n" +
            "      INFORMATION_SCHEMA.KEY_COLUMN_USAGE" + "\r\n" +
            "WHERE" + "\r\n" +
            "      TABLE_SCHEMA = ?DatabaseName" + "\r\n" +
            "      AND REFERENCED_TABLE_NAME IS NOT NULL";

    private static final String ALL_TABLES_SQL = "SELECT" + "\r\n" +
            "      TABLE_NAME as Table_Name" + "\r\n" +
            "FROM" + "\r\n" +
            "      INFORMATION_SCHEMA.TABLES" + "\r\n" +
            "WHERE" + "\r\n" +
            "      TABLE_SCHEMA = ?DatabaseName";

    private static final String MANY_TO_MANY_CHECK_ALL = "SELECT " + "\r\n" +
            "    FK.TABLE_NAME FK_Table, " + "\r\n" +
            "    KC.COLUMN_NAME FK_Column," + "\r\n" +
            "    KC.REFERENCED_TABLE_NAME PK_Table," + "\r\n" +
            "    KC.REFERENCED_COLUMN_NAME PK_Column,     " + "\r\n" +
            "    FK.CONSTRAINT_NAME  Constraint_Name    " + "\r\n" +
            "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS FK  " + "\r\n" +
            "INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE KC ON KC.CONSTRAINT_NAME  = FK.CONSTRAINT_NAME" + "\r\n" +
            "AND KC.TABLE_NAME = FK.TABLE_NAME AND KC.TABLE_SCHEMA = FK.TABLE_SCHEMA" + "\r\n" +
            "AND FK.TABLE_SCHEMA = ?DatabaseName" + "\r\n" +
            "AND FK.CONSTRAINT_TYPE = 'FOREIGN KEY'" + "\r\n" +
            "JOIN    (" + "\r\n" +
            "        SELECT tc.TABLE_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS tc" + "\r\n" +
            "        JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS kcu ON tc.Constraint_name = kcu.Constraint_Name AND kcu.TABLE_NAME = tc.TABLE_NAME AND kcu.TABLE_SCHEMA = tc.TABLE_SCHEMA" + "\r\n" +
            "        AND tc.Constraint_Type = 'PRIMARY KEY' " + "\r\n" +
            "        AND tc.TABLE_SCHEMA = ?DatabaseName" + "\r\n" +
            "        JOIN " + "\r\n" +
            "        (" + "\r\n" +
            "            SELECT tc1.Table_Name, kcu1.Column_Name AS Column_Name FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS tc1" + "\r\n" +
            "            JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS kcu1 ON tc1.Constraint_name = kcu1.Constraint_Name AND kcu1.TABLE_NAME = tc1.TABLE_NAME AND kcu1.TABLE_SCHEMA = tc1.TABLE_SCHEMA" + "\r\n" +
            "            AND tc1.Constraint_Type = 'FOREIGN KEY' " + "\r\n" +
            "            AND tc1.TABLE_SCHEMA = ?DatabaseName" + "\r\n" +
            "        ) " + "\r\n" +
            "        AS t ON t.Table_Name = tc.table_Name AND t.Column_Name = kcu.Column_Name  " + "\r\n" +
            "       " + "\r\n" +
            "        GROUP BY tc.Constraint_Name, tc.Table_Name HAVING COUNT(*) > 1" + "\r\n" +
            "        ) AS ManyMany ON ManyMany.TABLE_NAME = FK.TABLE_NAME";

    private static final String MANY_TO_MANY_FOREIGN_MAP_ALL = "SELECT " + "\r\n" +
            "    FK.TABLE_NAME FK_Table, " + "\r\n" +
            "    KC.COLUMN_NAME FK_Column," + "\r\n" +
            "    KC.REFERENCED_TABLE_NAME PK_Table," + "\r\n" +
            "    KC.REFERENCED_COLUMN_NAME PK_Column,     " + "\r\n" +
            "    FK.CONSTRAINT_NAME  Constraint_Name    " + "\r\n" +
            "FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS FK  " + "\r\n" +
            "INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE KC ON KC.CONSTRAINT_NAME  = FK.CONSTRAINT_NAME" + "\r\n" +
            "AND KC.TABLE_NAME = FK.TABLE_NAME AND KC.TABLE_SCHEMA = FK.TABLE_SCHEMA" + "\r\n" +
            "AND FK.TABLE_SCHEMA = ?DatabaseName" + "\r\n" +
            "AND FK.CONSTRAINT_TYPE = 'FOREIGN KEY'";

    public MysqlInnoDBSchemaReader(QueryHelper queryHelper) {
        super(queryHelper);
    }

    @Override
    public List<String> getSPList() {
        throw new UnsupportedOperationException();
    }
}
