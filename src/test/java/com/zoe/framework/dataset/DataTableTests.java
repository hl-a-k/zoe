package com.zoe.framework.dataset;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/10/1.
 */
public class DataTableTests {

    @Test
    public void Test01() throws Exception {
        //  Create meta-definition
        String[] columnNames = new String[]{"account_id", "owner", "balance", "rate"};
        Class[] columnTypes = new Class[]{String.class, String.class, Double.class, Double.class};
        String[] primaryKeys = new String[]{"account_id"};
        CRowMetaData metaDef = new CRowMetaData(columnNames, columnTypes, primaryKeys);

        //  Create a row of data
        CDataRow row = new CDataRow();
        row.setRawData(new Object[]{"asdf1234", "Casper", new Double(25000000.0), new Double(0.650434)});

        //  Create data container , set data
        CDataCacheContainer container = new CDataCacheContainer("mytest", metaDef, new HashMap());
        container.addData(new CDataRow[]{row});


        Assert.assertEquals(1,container.getNumberRows());
    }
}
