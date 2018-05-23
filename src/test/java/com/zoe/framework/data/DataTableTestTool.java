/**
 * 
 * DataTableTest.java Create on Jul 3, 2012 4:54:19 PM
 * 
 * @author James Cheung
 * @version 1.0
 *          Copyright (c) 2012 Metarace,Inc. All Rights Reserved.
 */
package com.zoe.framework.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author James Cheung
 * 
 */
public class DataTableTestTool {

    /**
     * 创建一个数据表对象，包括100个字段和1000条测试数据
     * 
     * @return 返回一个用于测试的DataTable对象
     * 
     * @author James Cheung
     *         Date:Jul 3, 2012
     * @throws Exception
     */
    public static DataTable createTestTable() throws Exception {
        DataTable table = new DataTable("DataCacheTestTable");
        List<DataColumn> keyColumns = new ArrayList<DataColumn>();
        for (int i = 0; i < 100; i++) {
            DataColumn col = table.addColumn("COL" + i, DataTypes.DATATABLE_STRING);
            if (i == 0) keyColumns.add(col);
        }
        DataKey key = new DataKey();
        key.setKeyColumns(keyColumns);
        table.setPrimeKey(key);
        // 加入索引列1
        key = new DataKey();
        keyColumns = new ArrayList<DataColumn>();
        keyColumns.add(table.getColumns().get(2));
        key.setKeyColumns(keyColumns);
        key.setKeyName("DataIndex_Key_1");
        table.getDataIndexs().add(key);
        // 加入索引列2
        key = new DataKey();
        keyColumns = new ArrayList<DataColumn>();
        keyColumns.add(table.getColumns().get(3));
        key.setKeyColumns(keyColumns);
        key.setKeyName("DataIndex_Key_2");
        table.getDataIndexs().add(key);
        return table;
    }

    public static void fillTable(DataTable table) throws Exception {
        for (int j = 0; j < 100; j++) {//1000
            DataRow row = table.newRow();
            for (int i = 0; i < 100; i++) {
                row.setValue(i, j * i + j + i);
            }
            table.addRow(row);
        }
    }

    public static void printTable(DataTable table) {
        for (int i = 0; i < table.getTotalCount(); i++) {
            for (int j = 0; j < table.getColumns().size(); j++) {
                System.out.print(table.getValue(i, j));
                if (j < 19) System.out.print(" , ");
            }
            System.out.print("\n");
        }
    }

    public static void fillTable(DataTable table, String preTag) {
        try {
            // 填充20列数据
            table.addColumn("LAC", DataTypes.DATATABLE_STRING);
            table.addColumn("CI", DataTypes.DATATABLE_STRING);
            table.addColumn("SCAN_TIME", DataTypes.DATATABLE_DATE);
            for (int i = 0; i < 20; i++) {
                table.addColumn(preTag + i, i % 13);
            }
            // 填充100行数据
            for (int i = 0; i < 100; i++) {
                DataRow row = table.newRow();
                row.setValue("lac", "lac" + i % 5);
                row.setValue("ci", "ci" + i % 5);
                row.setValue("SCAN_TIME", "2011-05-" + i % 25 + " 12:00:00");
                for (int j = 3; j < 23; j++) {
                    // 循环每列加入数据
                    row.setValue(j, i * j);
                }
                table.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于生成测试数据项的函数
     * 
     * @param i
     *            需要获取数据值的单元格编码
     * @return
     * 
     * @author James Cheung
     *         Date:Jul 3, 2012
     */
    public static String getValue(int i) {
        if (i % 7 == 0) return "";
        if (i % 5 == 0) return DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        return Convert.toString(i);
    }
}
