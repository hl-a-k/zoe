package com.zoe.framework.db.schema.reader;


import com.zoe.framework.db.schema.ServerType;
import com.zoe.framework.db.schema.query.QueryHelper;
import com.zoe.framework.db.schema.reader.ISchemaReader;

/**
 * Created by caizhicong on 2016/3/21.
 */
public class SchemaFactory {

    public static ISchemaReader getReader(ServerType serverType, QueryHelper queryHelper) {
        switch (serverType) {
            case Oracle:
                return new OracleSchemaReader(queryHelper);
            case MySQL:
                return new MysqlSchemaReader(queryHelper);
            default:
                return new OracleSchemaReader(queryHelper);
        }
    }

    /*public static ISchemaReader GetReader(ServerType serverType, String url, String user, String pass) {
        switch (serverType) {
            case Oracle:
                return new OracleSchemaReader(url, user, pass);
            case SqlServer:
                return new SqlServerSchemaReader(url, user, pass);
            case MySQL:
                return new MysqlSchemaReader(url, user, pass);
            case SQLite:
                return new SqliteSchemaReader(url, user, pass);
            default:
                return new OracleSchemaReader(url, user, pass);
        }
    }*/
}
