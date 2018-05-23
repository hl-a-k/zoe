package com.zoe.framework.db.schema.generator;

import com.zoe.framework.db.schema.Column;
import com.zoe.framework.db.schema.DbType;
import com.zoe.framework.db.schema.IStoredProcedure;
import com.zoe.framework.db.schema.Table;
import com.zoe.framework.db.schema.conversion.MySqlConvert;
import com.zoe.framework.db.schema.generator.ANSISchemaGenerator;
import org.apache.commons.lang3.StringUtils;

/**
 * mysql schema 生成类
 * Created by caizhicong on 2016/5/14.
 */
public class MySqlSchemaGenerator extends ANSISchemaGenerator {

    public MySqlSchemaGenerator() {
        ADD_COLUMN = "ALTER TABLE `%1$s` ADD `%2$s`%3$s;";
        ALTER_COLUMN = "ALTER TABLE `%1$s` MODIFY `%2$s`%3$s;";
        CREATE_TABLE = "CREATE TABLE `%1$s` (%2$s \r\n) ";
        DROP_COLUMN = "ALTER TABLE `%1$s` DROP COLUMN `%2$s`;";
        DROP_TABLE = "DROP TABLE %1$s;";

        UPDATE_DEFAULTS = "UPDATE `%1$s` SET `%2$s`=%3$s;";

        ADD_INDEX = "ALTER TABLE `%1$s` ADD %2$s (%3$s) ";
    }

    @Override
    public String QualifyTableName(Table tbl) {
        String qualifiedFormat = StringUtils.isBlank(tbl.getSchemaName()) ? "`%2$s`" : "`%1$s`.`%2$s`";
        String qualifiedTable = String.format(qualifiedFormat, tbl.getSchemaName(), tbl.getName());
        return qualifiedTable;
    }

    public String QualifyColumnName(Column column) {
        String qualifiedFormat = StringUtils.isBlank(column.getTable().getSchemaName()) ? "`%2$s`.`%3$s`" : "`%1$s`.`%2$s`.`%3$s`";
        return String.format(qualifiedFormat, column.getTable().getSchemaName(), column.getTable().getName(), column.getName());
    }

    @Override
    public String QualifySPName(IStoredProcedure sp) {
        String qualifiedFormat = "`%1$s`.`%2$s`";
        return String.format(qualifiedFormat, sp.getSchemaName(), sp.getName());
    }

    public String QualifyName(String name) {
        String qualifiedFormat = "`%1$s`";
        return String.format(qualifiedFormat, name);
    }

    /**
     * Builds a CREATE TABLE statement.
     *
     * @param table
     * @return String
     */
    @Override
    public String BuildCreateTableStatement(Table table) {
        String result = super.BuildCreateTableStatement(table);
        result += "\r\nENGINE=InnoDB DEFAULT CHARSET=utf8";
        if (table.getComment() != null) {
            result += " COMMENT='" + table.getComment().replace("'", "＇") + "'";
        }
        return result;
    }

    /**
     * Generates the columns.
     *
     * @param table Table containing the columns.
     * @return SQL fragment representing the supplied columns.
     */
    @Override
    public String GenerateColumns(Table table) {
        StringBuilder createSql = new StringBuilder();

        for (Column col : table.getColumns()) {
            createSql.append(String.format("\r\n  `%s`%s,", col.getName(), GenerateColumnAttributes(col)));
        }
        String columnSql = createSql.toString();
        return Chop(columnSql, ",");
    }

    /**
     * Sets the column attributes.
     *
     * @param column The column.
     * @return String
     */
    @Override
    public String GenerateColumnAttributes(Column column) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(GetNativeType(column.getDbType()));
        if (column.getDbType() == DbType.FLOAT || column.getDbType() == DbType.DOUBLE
                || column.getDbType() == DbType.DECIMAL
                || column.getDbType() == DbType.NUMERIC)
            sb.append("(").append(column.getPrecision()).append(", ").append(column.getScale()).append(")");
        else if (column.getDbType().isDate()) {
            if (column.getDbType() == DbType.YEAR) {
                sb.append("(4)");//year长度固定为4
            } else {
                //date、time、datetime、timestamp 日期类型不用加长度，如果加长度最大也只能是6
                //如果长度为6，则time可能如：10:38:54.000000，datetime、timestamp 同样如此。一般不需要设置为0即可
                int len = column.getLength() > 6 ? 6 : column.getLength();//长度最多为6
                if (column.getDbType() == DbType.DATE) len = 0; //date类型不能有长度，否则出错
                if (len > 0) {
                    sb.append("(").append(len).append(")");
                }
            }
        }
        //这边对那些不需要设置长度的类型的过滤，如 text、longtext、blob、longblob 等
        else if (column.getDbType().isNoNeedLength()) {
            sb.append("");
        } else if (column.getLength() > 0)
            sb.append("(").append(column.getLength()).append(")");
        if (column.isPrimaryKey())
            sb.append(" PRIMARY KEY ");

        if (column.isPrimaryKey() | !column.isNullable())
            sb.append(" NOT NULL ");

        if (column.isPrimaryKey() && column.getDbType().isNumeric() && column.isAutoIncrement())
            sb.append(" auto_increment ");

        if (column.getDefaultSetting() != null)
            sb.append(" DEFAULT '").append(column.getDefaultSetting()).append("'");

        if (column.getComment() != null) {
            sb.append(" COMMENT '").append(column.getComment().replace("'", "＇")).append("'");
        }

        return sb.toString();
    }

    /**
     * Gets the type of the db.
     *
     * @param sqlType Type of MySQL.
     * @return DbType
     */
    @Override
    public DbType GetDbType(String sqlType) {
        return MySqlConvert.GetDbType(sqlType);
    }

    /**
     * Gets the type of the native.
     *
     * @param dbType Type of the db.
     * @return db native type
     */
    @Override
    public String GetNativeType(DbType dbType) {
        return MySqlConvert.GetNativeType(dbType);
    }
}
