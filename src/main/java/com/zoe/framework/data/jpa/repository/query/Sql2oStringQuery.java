package com.zoe.framework.data.jpa.repository.query;

import org.springframework.util.Assert;

/**
 * Created by caizhicong on 2017/7/10.
 */
public class Sql2oStringQuery {
    private final String query;
    private final String alias;

    public Sql2oStringQuery(String query) {
        Assert.hasText(query, "Query must not be null or empty!");
        this.query = query;
        this.alias = Sql2oQueryUtils.detectAlias(query);
    }

    public String getQueryString() {
        return this.query;
    }

    public String getAlias() {
        return this.alias;
    }
}
