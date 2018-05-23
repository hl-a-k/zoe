package com.zoe.framework.sql2o.paging;

import com.zoe.framework.regex.Group;
import com.zoe.framework.regex.Match;
import com.zoe.framework.regex.Regex;
import com.zoe.framework.regex.RegexOptions;
import com.zoe.framework.sql2o.Sql2oException;
import org.springframework.util.StringUtils;

public class PagingHelper implements IPagingHelper{

    public Regex rxColumns = new Regex(
            "\\A\\s*SELECT\\s+((?:\\((?>\\((?<depth>)|\\)(?<-depth>)|.?)*(?(depth)(?!))\\)|.)*?)(?<!,\\s+)\\bFROM\\b",
            RegexOptions.forValue( RegexOptions.IgnoreCase.getValue() | RegexOptions.Singleline.getValue() | RegexOptions.Multiline.getValue()));

    public Regex rxOrderBy = new Regex(
            "\\bORDER\\s+BY\\s+(?!.*?(?:\\)|\\s+)AS\\s)(?:\\((?>\\((?<depth>)|\\)(?<-depth>)|.?)*(?(depth)(?!))\\)|[\\w\\(\\)\\.])+(?:\\s+(?:ASC|DESC))?(?:\\s*,\\s*(?:\\((?>\\((?<depth>)|\\)(?<-depth>)|.?)*(?(depth)(?!))\\)|[\\w\\(\\)\\.])+(?:\\s+(?:ASC|DESC))?)*",
            RegexOptions.forValue(RegexOptions.RightToLeft.getValue() | RegexOptions.IgnoreCase.getValue() | RegexOptions.Singleline.getValue()
                    | RegexOptions.Multiline.getValue()));

    //SUPPORT [NULLS FIRST | NULLS LAST]
    public static Regex rxOrderByOracle =  new Regex(
            "\\bORDER\\s+BY\\s+(?!.*?(?:\\)|\\s+)AS\\s)(?:\\((?>\\((?<depth>)|\\)(?<-depth>)|.?)*(?(depth)(?!))\\)|[\\w\\(\\)\\.])+(?:\\s+(?:ASC|DESC))?(?:\\s+NULLS\\s+(?:FIRST|LAST))?(?:\\s*,\\s*(?:\\((?>\\((?<depth>)|\\)(?<-depth>)|.?)*(?(depth)(?!))\\)|[\\w\\(\\)\\.])+(?:\\s+(?:ASC|DESC))?(?:\\s+NULLS\\s+(?:FIRST|LAST))?)*",
            RegexOptions.forValue(RegexOptions.RightToLeft.getValue() | RegexOptions.IgnoreCase.getValue() | RegexOptions.Singleline.getValue()
                    | RegexOptions.Multiline.getValue()));

    public Regex rxDistinct = new Regex("\\ADISTINCT\\s",
            RegexOptions.forValue(RegexOptions.IgnoreCase.getValue() | RegexOptions.Singleline.getValue() | RegexOptions.Multiline.getValue()));

    public Regex OrderByAlias = new Regex("\\w*\\.", RegexOptions.forValue(RegexOptions.IgnoreCase.getValue()
            | RegexOptions.Singleline.getValue() | RegexOptions.Multiline.getValue()));

    public PagingHelper(){

    }

    public SQLParts SplitSQL(String sql) {
        try {
            SQLParts parts = new SQLParts();
            sql = StringUtils.trimTrailingCharacter(sql.trim(), ';');
            parts.sql = sql;
            parts.sqlSelectRemoved = null;
            parts.sqlCount = null;
            parts.sqlOrderBy = null;

            // Extract the columns from "SELECT <whatever> FROM"
            Match m = rxColumns.Match(sql);
            if (!m.getSuccess())
                return null;

            // Save column list and replace with COUNT(*)
            Group g = m.getGroups().GetGroup(1);
            parts.sqlSelectRemoved = sql.substring(g.getIndex());

            if (rxDistinct.IsMatch(parts.sqlSelectRemoved))
                parts.sqlCount = sql.substring(0, g.getIndex()) + "COUNT("
                        + m.getGroups().GetGroup(1).toString().trim() + ") "
                        + sql.substring(g.getIndex() + g.getLength());
            else {
                parts.sqlCount = sql.substring(0, g.getIndex()) + "COUNT(*) "
                        + sql.substring(g.getIndex() + g.getLength());
                if (parts.sqlCount.toLowerCase().contains("group by")){
                    parts.sqlCount = "select count(*) from ( "+parts.sqlCount+" ) countAlis";
                }
            }
            // Look for the last "ORDER BY <whatever>" clause not part of a
            m = rxOrderBy.Match(parts.sqlCount);
            if (m.getSuccess()) {
                g = m.getGroups().GetGroup(0);
                parts.sqlOrderBy = g.toString();
                parts.sqlCount = parts.sqlCount.substring(0, g.getIndex())
                        + parts.sqlCount
                        .substring(g.getIndex() + g.getLength());
            }
            return parts;
        } catch (Exception ex) {
            throw new Sql2oException(
                    "Unable to parse SQL statement for paged query");
        }
    }

    public Regex getRxColumns(){
        return rxColumns;
    }

    public Regex getRxOrderBy(){
        return rxOrderBy;
    }

    public Regex getRxDistinct(){
        return rxDistinct;
    }
}
