package com.zoe.framework.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

public class DataColumnCollection implements List<DataColumn> {
    // key值为列头小写，value为该列所处的位置
    private LinkedHashMap<String, DataColumn> nameMap;
    // 存放DataColumn对象
    private List<DataColumn> columns;

    /**
     * 创建一个新的实例 DataColumnCollection.
     *
     */
    public DataColumnCollection() {
        this.nameMap = new LinkedHashMap<String, DataColumn>();
        this.columns = new Vector<DataColumn>();
    }

    /**
     * 功能描述： 根据列名取得该列名所处的列位置
     * 
     * @param
     * @return: int
     */
    public int getColumnIndex(String columnName) {
        if (this.nameMap.containsKey(columnName.toLowerCase().trim())) {
            DataColumn column = nameMap.get(columnName.toLowerCase());
            return column.getColumnIndex();
        }
        return -1;
    }

    /**
     * 功能描述： 返回DataTable的实际列数
     * 
     * @param
     * @return: int
     */
    public int size() {
        return this.columns.size();
    }

    /**
     * 功能描述： 清空columns和nameMap
     * 
     * @param
     * @return: void
     */
    public void clear() {
        this.columns.clear();
        this.nameMap.clear();
    }

    /**
     * 功能描述： 给columns加入一列
     * 
     * @param
     * @return: boolean
     */
    public boolean add(DataColumn column) {
        // 判断指定列是否已存在
        if (!this.nameMap.containsKey(column.getColumnName().toLowerCase())) {
            column.setColumnIndex(this.columns.size());
            boolean res = this.columns.add(column);
            this.nameMap.put(column.getColumnName().toLowerCase(), column);
            return res;
        }
        return false;
    }

    /**
     * 功能描述： 在指定位置 给columns加入一列并同时修改所对应的nameMap集合
     * 
     * @param index
     * @param column
     * @return: void
     */
    public void add(int index, DataColumn column) {
        // 判断指定列是否已存在
        if (!this.nameMap.containsKey(column.getColumnName().toLowerCase())) {
            this.columns.add(index, column);
            column.setColumnIndex(index);
            this.nameMap.put(column.getColumnName().toLowerCase(), column);
        }
        for (int i = index + 1; i < this.columns.size(); i++) {
            DataColumn dataColumn = this.columns.get(i);
            dataColumn.setColumnIndex(dataColumn.getColumnIndex() + 1);
            this.columns.set(i, dataColumn);
        }
    }

    /**
     * 功能描述： 在指定位置 给columns加入一列并同时修改所对应的nameMap集合
     * 
     * @param index
     * @param column
     * @return: void
     */
    public boolean addDataColumn(int index, DataColumn column) {
        // 判断指定列是否已存在
        if (!this.nameMap.containsKey(column.getColumnName().toLowerCase())) {
            this.columns.add(index, column);
            column.setColumnIndex(index);
            this.nameMap.put(column.getColumnName().toLowerCase(), column);

            for (int i = index + 1; i < this.columns.size(); i++) {
                DataColumn dataColumn = this.columns.get(i);
                dataColumn.setColumnIndex(dataColumn.getColumnIndex() + 1);
                this.columns.set(i, dataColumn);
            }
            return true;
        }
        return false;
    }

    /**
     * 功能描述： 删除columns的一列数据
     * 
     * @param
     * @return: boolean
     */
    public boolean remove(DataColumn column) {
        boolean res = false;
        if (this.nameMap.containsKey(column.getColumnName().toLowerCase())) {
            res = this.columns.remove(column);
            this.nameMap.remove(column.getColumnName().toLowerCase());
        }
        return res;
    }

    /**
     * 功能描述： 删除columns的指定列数据
     * 
     * @param
     * @return: DataColumn
     */
    public DataColumn remove(int index) {
        DataColumn column = this.get(index);
        // 判断指定列是否已存在
        if (this.nameMap.containsKey(column.getColumnName().toLowerCase())) {
            {
                this.nameMap.remove(column.getColumnName().toLowerCase());
                return this.columns.remove(index);
            }
        }
        return null;
    }

    /**
     * 功能描述： 根据列名删除columns的指定列数据
     * 
     * @param
     * @return: DataColumn
     */
    public void remove(String columnName) {
        int tempIndex = getColumnIndex(columnName.toLowerCase());
        if (tempIndex > -1) {
            remove(tempIndex);
        }
    }

    /**
     * 功能描述： 得到指定列位置数据
     * 
     * @param
     * @return: DataColumn
     */
    public DataColumn get(int index) {
        return this.columns.get(index);
    }

    /**
     * 功能描述： 得到指定列名称的数据
     * 
     * @param
     * @return: DataColumn
     */
    public DataColumn get(String columnName) {
        if (this.nameMap.containsKey(columnName.toLowerCase())) return this.nameMap.get(columnName.toLowerCase());
        return null;
    }

    @Override
    public boolean isEmpty() {
        return this.columns.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.columns.contains(o);
    }

    @Override
    public Iterator<DataColumn> iterator() {
        return this.columns.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.columns.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.columns.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return this.columns.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.columns.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends DataColumn> c) {
        return this.columns.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends DataColumn> c) {
        return this.columns.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.columns.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.columns.retainAll(c);
    }

    @Override
    public DataColumn set(int index, DataColumn element) {
        return this.columns.set(index, element);
    }

    @Override
    public int indexOf(Object o) {
        return this.columns.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.columns.lastIndexOf(o);
    }

    @Override
    public ListIterator<DataColumn> listIterator() {
        return this.columns.listIterator();
    }

    @Override
    public ListIterator<DataColumn> listIterator(int index) {
        return this.columns.listIterator(index);
    }

    @Override
    public List<DataColumn> subList(int fromIndex, int toIndex) {
        return this.columns.subList(fromIndex, toIndex);
    }

    public DataColumn addColumn(String columnName, int dataType) throws Exception {
        DataColumn col = new DataColumn(columnName, dataType);
        col.setCaptionName(columnName);
        if (this.add(col)) return col;
        if (contains(columnName)) throw new Exception("指定名称的列已经存在," + columnName);
        return null;
    }

    public DataColumn addColumn(int index, String columnName, int dataType) throws Exception {
        DataColumn col = new DataColumn(columnName, dataType);
        col.setCaptionName(columnName);
        if (this.addDataColumn(index, col)) return col;
        if (contains(columnName)) throw new Exception("指定名称的列已经存在," + columnName);
        return null;
    }

    public boolean contains(String columnName) {
        return this.nameMap.containsKey(columnName.toLowerCase());
    }
}
