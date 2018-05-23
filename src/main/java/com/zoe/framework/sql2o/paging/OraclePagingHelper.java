package com.zoe.framework.sql2o.paging;

import com.zoe.framework.regex.Regex;
import com.zoe.framework.regex.RegexOptions;

/**
 * Created by caizhicong on 2016/7/28.
 */
public class OraclePagingHelper extends PagingHelper{

    //oracle独有的nulls first跟nulls last支持
    private Regex rxOrderByOracle = new Regex(
            "\\bORDER\\s+BY\\s+(?!.*?(?:\\)|\\s+)AS\\s)(?:\\((?>\\((?<depth>)|\\)(?<-depth>)|.?)*(?(depth)(?!))\\)|[\\w\\(\\)\\.])+(?:\\s+(?:ASC|DESC))?(?:\\s+NULLS\\s+(?:FIRST|LAST))?(?:\\s*,\\s*(?:\\((?>\\((?<depth>)|\\)(?<-depth>)|.?)*(?(depth)(?!))\\)|[\\w\\(\\)\\.])+(?:\\s+(?:ASC|DESC))?(?:\\s+NULLS\\s+(?:FIRST|LAST))?)*",
            RegexOptions.forValue(RegexOptions.RightToLeft.getValue() | RegexOptions.IgnoreCase.getValue() | RegexOptions.Singleline.getValue()
                    | RegexOptions.Multiline.getValue()));

    public OraclePagingHelper(){
        rxOrderBy = rxOrderByOracle;
    }
}
