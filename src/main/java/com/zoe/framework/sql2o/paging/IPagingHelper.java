package com.zoe.framework.sql2o.paging;

import com.zoe.framework.regex.Regex;

/**
 * Created by caizhicong on 2016/7/28.
 */
public interface IPagingHelper {


    Regex getRxColumns();

    Regex getRxOrderBy();

    Regex getRxDistinct();

    /**
     * 切分SQL
     *
     * @param sql sql语句
     * @return
     */
    SQLParts SplitSQL(String sql);
}
