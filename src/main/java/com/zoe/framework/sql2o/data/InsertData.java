package com.zoe.framework.sql2o.data;

import java.util.Map;

public class InsertData {

	private String sql;

	private Map<String, Object> parameters;

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameter(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
