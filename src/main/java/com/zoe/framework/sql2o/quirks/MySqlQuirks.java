package com.zoe.framework.sql2o.quirks;

import com.zoe.framework.sql2o.converters.Converter;
import com.zoe.framework.sql2o.paging.IPagingHelper;
import com.zoe.framework.sql2o.paging.PagingHelper;

import java.util.Map;

/**
 * @author caizhicong
 * @since 2015-06-08
 */
public class MySqlQuirks extends NoQuirks {

    public MySqlQuirks() {
        super();
    }

    public MySqlQuirks(Map<Class, Converter> converters) {
        super(converters);
    }

    @Override
    public String getNowSql() {
        return "select now()";
    }

    /**
     * 获取数据库类型
     * @return 数据库类型
     */
    @Override
    public ServerType getServerType() {
        return ServerType.MySQL;
    }
}
