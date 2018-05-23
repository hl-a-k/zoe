package com.zoe.framework.sql2o.data;

import com.zoe.framework.sql2o.tools.AbstractCache;

public class PojoData {

	private static final AbstractCache<Class<?>, PojoData, Void> rpCache = new AbstractCache<Class<?>, PojoData, Void>() {
		@Override
		protected PojoData evaluate(Class<?> key, Void param) {
			return new PojoData(key);
		}
	};

	private final Class<?> clazz;

	public Class<?> getPojoClass() {
		return clazz;
	}

	PojoData(Class<?> cls) {
		clazz = cls;

		// Get the table info
		tableInfo = TableInfo.fromPoco(cls);
	}

	public static PojoData forClass(Class<?> cls) {
		return rpCache.get(cls, null);
	}

	public static PojoData removeClass(Class<?> cls) {
		return rpCache.remove(cls);
	}

	private TableInfo tableInfo;

	public final TableInfo getTableInfo() {
		return tableInfo;
	}

	/**
	 * 缓存根据主键查询实体的语句
	 */
	private String SqlGetByID;

	public final String getSqlGetByID() {
		return SqlGetByID;
	}

	public final void setSqlGetByID(String value) {
		SqlGetByID = value;
	}

	/**
	 * 缓存根据主键物理删除实体的语句
	 */
	private String SqlDeleteByID;

	public final String getSqlDeleteByID() {
		return SqlDeleteByID;
	}

	public final void setSqlDeleteByID(String value) {
		SqlDeleteByID = value;
	}

	/**
	 * 缓存根据主键逻辑删除实体的语句
	 */
	private String SqlLogicalDeleteByID;

	public final String getSqlLogicalDeleteByID() {
		return SqlLogicalDeleteByID;
	}

	public final void setSqlLogicalDeleteByID(String value) {
		SqlLogicalDeleteByID = value;
	}
}
