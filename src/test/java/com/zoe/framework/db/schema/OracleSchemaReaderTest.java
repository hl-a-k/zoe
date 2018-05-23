package com.zoe.framework.db.schema;

import com.zoe.framework.db.schema.query.QueryHelper;
import com.zoe.framework.db.schema.reader.ISchemaReader;
import com.zoe.framework.db.schema.reader.OracleSchemaReader;
import com.zoe.framework.db.schema.BaseTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by caizhicong on 2016/5/20.
 */
public class OracleSchemaReaderTest extends BaseTest {

    @Autowired
    private QueryHelper queryHelper;

    ISchemaReader schemaReader;

    @Before
    public void setUp() throws Exception {
        schemaReader = new OracleSchemaReader(queryHelper);
    }

    @Test
    public void Test01() {
        List<String> owners = schemaReader.getOwners();
        Assert.assertTrue(owners.size() > 0);
    }

    @Test
    public void Test02() {
        long t1 = System.currentTimeMillis();
        List<String> tableNameList = schemaReader.getTableNameList("ZOEDDC");
        long t2 = System.currentTimeMillis();
        System.out.println("fetch " + tableNameList.size() + " records takes(ms) :" + (t2 - t1));
        Assert.assertTrue(tableNameList.size() >= 0);

        List<String> viewNameList = schemaReader.getViewNameList("ZOEDDC");
        Assert.assertTrue(viewNameList.size() >= 0);
    }

    @Test
    public void Test03() {
        List<Table> tables = schemaReader.getTables("ZOEDDC");
        Assert.assertTrue(tables.size() >= 0);

        List<Table> views = schemaReader.getViews("ZOEDDC");
        Assert.assertTrue(views.size() >= 0);

        if (tables.size() > 0) {
            Table table = schemaReader.getTable("ZOEDDC", tables.get(0).getName());
            Assert.assertTrue(table != null);
        }

        if (views.size() > 0) {
            Table view = schemaReader.getView("ZOEDDC", views.get(0).getName());
            Assert.assertTrue(view != null);
        }
    }

    @Test
    public void Test04() {
        List<Column> columns1 = schemaReader.getColumns("ZOEDDC", "BASE_DICT");
        Assert.assertTrue(columns1.size() >= 0);

        List<Column> columns = schemaReader.getTableColumns(new Table("ZOEDDC", "BASE_DICT"));
        Assert.assertTrue(columns.size() >= 0);
    }
}
