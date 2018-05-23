package com.zoe.framework.sql2o.data;

import com.zoe.framework.sql2o.reflection.PojoProperty;

public class PocoColumn {

	public String ColumnName;
	public PojoProperty PropertyInfo;
	public boolean IsDateTime;
	public boolean IsPrimaryKey;
	public boolean hasColumnAnnotation = true;

	public void SetValue(Object target, Object val) {
		PropertyInfo.set(target, val);
	}

	public Object GetValue(Object target) {
		return PropertyInfo.get(target);
	}

	private boolean nullable = true;
	public void nullable(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean nullable() {
		return nullable;
	}

	private boolean insertable = true;
	public void insertable(boolean insertable) {
		this.insertable = insertable;
	}

	public boolean insertable() {
		return insertable;
	}

	private boolean updatable = true;
	public void updatable(boolean updatable) {
		this.updatable = updatable;
	}

	public boolean updatable() {
		return updatable;
	}
}
