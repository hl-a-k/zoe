package com.zoe.framework.data;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataTable {

    private static String SCAN_TIME = "SCAN_TIME";
    private DataRowCollection rows; // 用于保存DataRow的集合对象
    private DataColumnCollection columns; // 用于保存DataColumn的对象
    private String tableName; // 表名
    private String tableLocalName; // 表中文名
    private String dataSourceName; // 数据源名
    private DataKey primeKey = new DataKey();// 主键
    private List<DataKey> dataIndexs = new ArrayList<DataKey>();// 数据索引列表
    private Map<String, Object> tag = new HashMap<String, Object>(); // 做扩展用，（比如TOPN那边如果分页的话是用来作为分页使用的）
    private static Logger logger = LoggerFactory.getLogger(DataTable.class);

    public DataKey getPrimeKey() {
        return primeKey;
    }

    public void setPrimeKey(DataKey primeKey) {
        this.primeKey = primeKey;
    }

    public List<DataKey> getDataIndexs() {
        return dataIndexs;
    }

    public void setDataIndexs(List<DataKey> dataIndexs) {
        this.dataIndexs = dataIndexs;
    }

    public DataTable() {
        this.columns = new DataColumnCollection();
        this.rows = new DataRowCollection();
        this.rows.setColumns(columns);
    }

    public DataTable(String dataTableName) {
        this();
        this.tableName = dataTableName;
    }

    public int getTotalCount() {
        return rows.size();
    }

    /**
     * 功能描述： 返回表名
     * 
     * @param
     * @return: String
     * @author: guojiyong
     * @version: 2.0
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * 功能描述： 设置表名
     * 
     * @param
     * @return: void
     * @author: guojiyong
     * @version: 2.0
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 功能描述： 返回该表引用的封装类
     * 
     * @param
     * @return: DataRowCollection
     * @author: guojiyong
     * @version: 2.0
     */
    public DataRowCollection getRows() {
        return this.rows;
    }

    public DataColumnCollection getColumns() {
        return this.columns;
    }

    /**
     * 功能描述： 获取指定行指定列的数据
     * 
     * @param
     * @return: Object
     * @author: James Cheung
     * @version: 2.0
     */

    public Object getValue(int row, String colName) {
        return this.rows.get(row).getValue(colName);
    }

    public Object getValue(int row, int col) {
        return this.rows.get(row).getValue(col);
    }

    /**
     * 功能描述： 为该表数据新建一行
     * 
     * @param
     * @return: DataRow
     * @author: guojiyong
     * @version: 2.0
     */
    public DataRow newRow() throws Exception {
        DataRow tempRow = new DataRow(this);
        // nextRowIndex = nextRowIndex < this.rows.size() ? this.rows.size()
        // : nextRowIndex;
        int lastRowIndex = 0;
        if (this.rows.size() > 0) {
            lastRowIndex = this.rows.get(this.rows.size() - 1).getRowIndex();
        } else {
            lastRowIndex = 0;
        }

        tempRow.setColumns(this.columns);
        tempRow.setRowIndex(++lastRowIndex);
        return tempRow;
    }

    public void setValue(int row, int col, Object value) {
        this.rows.get(row).setValue(col, value);
    }

    public void setValue(int row, String colName, Object value) {
        this.rows.get(row).setValue(colName, value);
    }

    /**
     * @param name
     */
    public void setTag(String name, Object value) {
        this.tag.put(name, value);
    }

    /**
     * @return the tag
     */
    public Object getTag(String name) {
        return tag.get(name);
    }

    public DataColumn addColumn(String columnName, int dataType) throws Exception {
        return this.columns.addColumn(columnName, dataType);
    }

    public DataColumn addColumnIndex(int index, String columnName, int dataType) throws Exception {
        return this.columns.addColumn(index, columnName, dataType);

    }

    public boolean addRow(DataRow row) throws Exception {
        // if (row.getRowIndex() > this.rows.size())
        // row.setRowIndex(this.rows.size());
        // return this.rows.add(row);

        if (this.rows.size() > 0) {
            // if (row.getRowIndex() >
            // this.rows.get(this.rows.size()-1).getRowIndex() + 1) {
            row.setRowIndex(this.rows.get(this.rows.size() - 1).getRowIndex() + 1);
            // }
        } else {
            row.setRowIndex(1);
        }
        return this.rows.add(row);

    }

    // 以下为数据表扩展方法实现集合
    /**
     * 功能描述： 返回符合过滤条件的数据行集合，并返回
     * 
     * @param
     * @return: DataTable
     * @author: James Cheung
     * @version: 2.0
     */
    public DataTable select(String filterString) {
        List<DataRow> rows = new ArrayList<DataRow>();
        if (StringUtils.isNotEmpty(filterString)) {
            Object o = null;
            for (Object row : this.rows) {
                DataRow currentRow = (DataRow) row;

                try {
                    //TODO DSL
                    o = Boolean.TRUE;
                    //o = DSLUtil.getDefaultInstance().compute(filterString, currentRow.getItemMap());
                } catch (Exception e) {
                    System.out.println("DataTable select 执行错误，跳过执行");
                    continue;
                }
                if ((Boolean) o) {
                    rows.add(currentRow);
                }
            }
            DataTable tb = this.cloneTable();
            tb.getRows().addAll(rows);
            return tb;
        } else {
            return this;
        }
    }

    /**
     * 功能描述： 选择符合指定过滤条件的数据，并按照排序规则排序输出
     * 
     * @param orderBy
     *            lac,ci
     * @return: List<DataRow>
     * @author: James Cheung
     * @version: 2.0
     */
    public DataTable select(String filterString, String orderBy) {
        String[] orderFields = orderBy.split(",");
        List<SortedDataColumn> sortColumns = new ArrayList<SortedDataColumn>();
        for (String s : orderFields) {
            SortedDataColumn sort = new SortedDataColumn();
            if (s.trim().toLowerCase().endsWith("desc")) {
                sort.setSortType(SortType.DESC);
            } else {
                sort.setSortType(SortType.ASC);
            }
            s = s.replace("desc", "").replace("asc", "").trim();
            sort.setColumn(this.getColumns().get(s));
            sortColumns.add(sort);
        }
        // 首先过滤
        DataTable result = cloneTable();
        List<DataRow> dataRows = select(filterString).getRows();
        for (DataRow row : dataRows) {
            sort(result, row, sortColumns);
        }
        return result;
    }

    /**
     * 功能描述：
     * 
     * @param sort
     * @return: DataTable
     * @author: James Cheung
     * @version: 2.0
     */
    public DataTable sort(DataTable table, DataRow row, List<SortedDataColumn> sort) {
        if (table == null) return null;
        int tagetIndex = 0;
        for (DataRow r : table.rows) {
            // 循环现有行，针对个排序字段进行判断，判断规则为：当找到第一个返回TURE的记录的索引
            int compareResult = 0; // 默认按相等处理
            tagetIndex++;
            for (SortedDataColumn st : sort) {
                // 如果倒序
                int temp = 0;
                if (st.getSortType() == SortType.DESC) {
                    temp = compare(row.getValue(st.getColumn().getColumnName()), r.getValue(st.getColumn().getColumnName()));
                } else {
                    // 构造按正序
                    temp = compare(r.getValue(st.getColumn().getColumnName()), row.getValue(st.getColumn().getColumnName()));
                }
                if (temp < 0) {
                    compareResult = 0;
                    break;// 出现不满足条件记录，继续对比下一条
                } else {
                    compareResult = compareResult + temp;
                }
                if (compareResult > 0) // 如果找到了，则直接跳出
                {
                    tagetIndex--;
                    break;
                }
            }
            if (compareResult > 0) break;
        }
        table.getRows().add(tagetIndex, row);
        return table;
    }

    public DataTable cloneTable() {
        try {
            DataTable table = new DataTable();
            table.setTableName(this.getTableName());
            table.setDataSourceName(this.getDataSourceName());
            table.setTableLocalName(this.getTableLocalName());
            table.setPrimeKey(primeKey);
            table.setDataIndexs(getDataIndexs());
            for (DataColumn dc : this.columns) {
                DataColumn dcc = table.addColumn(dc.getColumnName(), dc.getDataType());
                dcc.setDisplayed(dc.isDisplayed());
            }
            return table;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 功能描述： 对当前表进行查询 过滤，并返回指定列集合拼装的DataTable对象
     * 
     * @param
     * @return: DataTable
     * @author: James Cheung
     * @version: 2.0
     * @throws Exception
     */
    public DataTable select(String filterString, String[] columns, boolean distinct) throws Exception {
        DataTable result = new DataTable();
        List<DataRow> rows = select(filterString).getRows();
        // 构造表结构
        for (String c : columns) {
            DataColumn dc = this.columns.get(c);
            DataColumn newDc = new DataColumn(dc.getColumnName(), dc.getDataType());
            newDc.setCaptionName(dc.getCaptionName());
            result.columns.add(newDc);
        }
        // 填充数据
        for (DataRow r : rows) {
            DataRow newRow = result.newRow();
            newRow.copyFrom(r);
            result.addRow(newRow);
        }
        return result;
    }

    // 静态方法集合
    public static Object newest(DataTable table, Object field, Object filter) {
        Object result = -99999999;
        DataTable dt = (DataTable) table;
        // 如果获取到数据表且表内包含时间字段
        if (dt.getColumns().contains(SCAN_TIME)) {
            Date lastTime = null;
            // 如果过滤条件为空
            filter = filter.toString().replace("[", "").replace("]", "");
            if (StringUtils.isEmpty(filter.toString())) {
                filter = "1>0"; // 设置过滤条件永远为true
            }
            List<DataRow> rows = dt.select(filter.toString()).getRows();
            int position = 0;
            Date newDate = null;
            for (DataRow row : rows) {
                // 如果过滤条件为空
                if (StringUtils.isEmpty(filter.toString())) {
                    filter = "1>0"; // 设置过滤条件永远为true
                }
                // 如果当前记录时间晚于当前记录时间
                if (position == 0) {
                    lastTime = DateUtil.getDate(row.getValue(SCAN_TIME).toString());
                    result = row.getValue(field.toString());
                } else {
                    newDate = DateUtil.getDate(row.getValue(SCAN_TIME).toString());
                    if (lastTime.before(newDate)) {
                        // 如果满足过滤条件
                        lastTime = newDate;
                        result = row.getValue(field.toString());
                    }
                }
                position++;

            }
        }
        return result == null ? -99999999 : result;
    }

    public static Object max(DataTable dt, Object field) {
        return max(dt, field, "");
    }

    public static Object max(DataTable dt, Object field, Object filter) {
        double result = -999999999;
        field = field.toString().replace("[", "").replace("]", "");
        DataTable table = (DataTable) dt;
        // 如果获取到数据表且表内包含时间字段
        if (table != null) {
            // 如果过滤条件为空
            filter = filter.toString().replace("[", "").replace("]", "");
            if (StringUtils.isEmpty(filter.toString())) {
                filter = "1>0"; // 设置过滤条件永远为true
            }
            List<DataRow> rows = table.select(filter.toString()).getRows();
            for (DataRow row : rows) {
                Object value = row.getValue(field.toString());
                if (value == null || value.toString().equals("")) {
                    continue;
                }
                double tmp = Convert.toDouble(value.toString());
                if (result < tmp) {
                    // 如果满足过滤条件
                    result = tmp;
                }
            }
        }
        return result;
    }

    public static Object min(Object dt, Object field) {
        return min(dt, field, "");
    }

    public static Object min(Object dt, Object field, Object filter) {
        double result = 999999999;
        field = field.toString().replace("[", "").replace("]", "");

        DataTable table = (DataTable) dt;
        // 如果获取到数据表且表内包含时间字段
        if (table != null) {
            // 如果过滤条件为空
            if (StringUtils.isEmpty(filter.toString())) {
                filter = "1>0"; // 设置过滤条件永远为true
            }
            List<DataRow> rows = table.select(filter.toString().replace("[", "").replace("]", "")).getRows();
            for (DataRow row : rows) {
                // 如果当前记录时间晚于当前记录时间
                Object value = row.getValue(field.toString());
                if (value == null || value.toString().equals("")) {
                    continue;
                }
                double tmp = Convert.toDouble(value.toString());

                if (result > tmp) {
                    // 如果满足过滤条件
                    result = tmp;
                }
            }
        }
        return result;
    }

    public static Object avg(Object dt, Object field) {
        return avg(dt, field, "");
    }

    public static Object avg(Object dt, Object field, Object filter) {
        double result = 0;//
        filter = filter.toString().replace("[", "").replace("]", "");
        field = field.toString().replace("[", "").replace("]", "");
        DataTable table = (DataTable) dt;
        // 如果获取到数据表且表内包含时间字段
        if (table != null) {
            // 如果过滤条件为空
            if (StringUtils.isEmpty(filter.toString())) {
                filter = "1>0"; // 设置过滤条件永远为true
            }
            List<DataRow> rows = table.select(filter.toString().replace("[", "").replace("]", "")).getRows();
            for (DataRow row : rows) {
                // 如果满足过滤条件
                Object value = row.getValue(field.toString());
                if (value == null || value.toString().equals("")) {
                    continue;
                }
                result += Convert.toDouble(value.toString());
            }
            if (rows.size() == 0) {
                result = 0;
            } else {
                result = result / rows.size();
            }
        }
        return result;
    }

    public static Object sum(Object dt, Object field) {
        return sum(dt, field, "");
    }

    public static Object sum(Object dt, Object field, Object filter) {
        field = field.toString().replace("[", "").replace("]", "");
        double result = 0.0;
        DataTable table = (DataTable) dt;
        // 如果获取到数据表且表内包含时间字段
        if (table != null) {
            // 如果过滤条件为空
            if (StringUtils.isEmpty(filter.toString())) {
                filter = "1>0"; // 设置过滤条件永远为true
            }
            List<DataRow> rows = table.select(filter.toString().replace("[", "").replace("]", "")).getRows();
            for (DataRow row : rows) {
                // 如果满足过滤条件
                Object value = row.getValue(field.toString());
                if (value == null || value.toString().equals("")) {
                    continue;
                }
                result += Convert.toDouble(value.toString());
            }
        }
        return result;
    }

    public static Object count(Object table, Object filter) {
        DataTable dt = (DataTable) table;
        if (dt.columns.contains(filter.toString().trim())) filter = "";
        return dt.select(filter.toString().replace("[", "").replace("]", "")).getRows().size();
    }

    public static Object count(Object table) {
        return count(table, "");
    }

    public Object compute(Object object) {
        if (object == null) return null;
        if (this.columns.contains(object.toString().trim().toLowerCase())) {
            if (this.getRows().size() > 0) { return this.getRows().get(0).getValue(object.toString()); }
        }
        String exp = object.toString();
        String regEx = "(max|min|avg|sum|count)[\\s]*\\(";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(exp);
        while (m.find()) {
            exp = exp.replace(m.group(0), m.group(0) + "table,");
        }
        //TODO DSL
        //return DSLUtil.getDefaultInstance().compute(exp, this);
        return null;
    }

    /**
     * 功能描述： 判断A的值是否大于B的值，如大于则返回true,否则返回false,
     * 返回0 标识两个值相等，1：a>b,-1:a<b
     * 
     * @param
     * @return: boolean
     * @author: James Cheung
     */
    public static int compare(Object a, Object b) {
        if (a == null) return -1;
        if (b == null) return 1;
        try {
            if (Convert.toDouble(a.toString()) > Convert.toDouble(b.toString())) return 1;
            else if (Convert.toDouble(a.toString()) < Convert.toDouble(b.toString())) return -1;
            else return 0;
        } catch (Exception ex) {
            try {
                if (DateUtil.getDate(a.toString()).after(DateUtil.getDate(b.toString()))) return 1;
                else if (DateUtil.getDate(a.toString()).equals(DateUtil.getDate(b.toString()))) return 0;
                else return -1;

            } catch (Exception e) {
                if (a.toString().compareTo(b.toString()) == 0) return 0;
                else if (a.toString().compareTo(b.toString()) > 0) return 1;
                else return -1;
            }
        }
    }

    public Object get(String varname) {
        if (varname.equals("table")) return this;
        return null;
    }

    public boolean has(String varName) {
        return varName.equals("table");
    }

    public Map getContextMap() {
        Map map = new HashMap<String, Object>();
        map.put("table", this);
        for (DataColumn dc : this.getColumns()) {
            map.put(dc.getColumnName().toLowerCase(), dc.getColumnName().toLowerCase());
        }
        return map;
    }

    public Object set(String varname, Object o) {
        return null;
    }

    /**
     * @return the tableLocalName
     */
    public String getTableLocalName() {
        return tableLocalName;
    }

    /**
     * @param tableLocalName
     */
    public void setTableLocalName(String tableLocalName) {
        this.tableLocalName = tableLocalName;
    }

    /**
     * @return the dataSourceName
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * @param dataSourceName
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }


    public void calculate(Collection<DataColumMapping> map) {
        // 首先检查数据表结构，加入当前数据表不存在的字段
        for (DataColumMapping m : map) {
            if (!getColumns().contains(m.getColumnId())) {
                try {
                    addColumn(m.getColumnId(), DataTypes.DATATABLE_STRING);
                } catch (Exception e) {
                    logger.error("TableCalculate Error：" + e.getMessage(), e);
                }
            }
        }
        // 循环计算数据行
        for (DataRow row : getRows()) {
            dataRowCalculate(row, map);
        }
    }

    public static void dataRowCalculate(DataRow row, Collection<DataColumMapping> map) {
        try {
            for (DataColumMapping m : map) {
                //TODO DSL
                //row.setValue(m.getColumnId(), DSLUtil.getDefaultInstance().compute(m.getGatherFormula(), row.getItemMap()));
            }
        } catch (Exception e) {
            logger.error("TableCalculate Error：" + e.getMessage(), e);
        }
    }
}
