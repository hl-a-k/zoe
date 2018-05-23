package com.zoe.framework.db.schema;

import com.zoe.framework.db.schema.generator.ISchemaGenerator;
import com.zoe.framework.db.schema.generator.MySqlSchemaGenerator;
import com.zoe.framework.db.schema.query.QueryHelper;
import com.zoe.framework.db.schema.reader.ISchemaReader;
import com.zoe.framework.db.schema.reader.MysqlSchemaReader;
import com.zoe.framework.db.schema.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caizhicong on 2016/5/23.
 */
public class MySqlSchemaGeneratorTest extends BaseTest {

    @Autowired
    QueryHelper queryHelper;

    ISchemaReader schemaReader;
    ISchemaGenerator schemaGenerator;

    @Before
    public void setUp() throws Exception {
        schemaGenerator = new MySqlSchemaGenerator();
        schemaReader = new MysqlSchemaReader(queryHelper);
    }

    @Test
    public void Test00(){
        //index Test
        Column column = new Column();
        column.setName("user_id");
        String addIndex1 = schemaGenerator.BuildAddIndexStatement("user", column, "idx_userid");
        String addIndex2 = schemaGenerator.BuildAddIndexStatement("user",column,null);
        column.setIsPrimaryKey(true);
        addIndex1 = schemaGenerator.BuildAddIndexStatement("user", column, "idx_userid");
        column.setIsPrimaryKey(false);
        column.setIsUnique(true);
        addIndex2 = schemaGenerator.BuildAddIndexStatement("user",column,null);

        List<Column> columns = new ArrayList<>();
        columns.add(column);
        Column column1 = new Column();
        column1.setName("user_name");
        columns.add(column1);
        column.setIsPrimaryKey(false);
        column.setIsUnique(false);
        addIndex2 = schemaGenerator.BuildAddIndexStatement("user",columns,null);
    }

    @Test
    public void Test01() {
        List<Table> tables = schemaReader.getTables("ZOEDDC");
        if (tables.size() > 0) {
            Table table = schemaReader.getTable("ZOEDDC", tables.get(0).getName());
            Assert.assertTrue(table != null);

            List<Column> columns = schemaReader.getColumns("ZOEDDC", tables.get(0).getName());
            Assert.assertTrue(columns.size() >= 0);

            table.setColumns(columns);
            String sql = schemaGenerator.BuildCreateTableStatement(table);
            Assert.assertNotNull(sql);

            sql = schemaGenerator.BuildDropTableStatement(table.getName());
            Assert.assertNotNull(sql);

            sql = schemaGenerator.BuildDropColumnStatement(table.getName(), columns.get(0).getName());
            Assert.assertNotNull(sql);

            sql = schemaGenerator.BuildAddColumnStatement(table.getName(), columns.get(0));
            Assert.assertNotNull(sql);

            sql = schemaGenerator.BuildAlterColumnStatement(columns.get(0));
            Assert.assertNotNull(sql);
        }
    }
}
