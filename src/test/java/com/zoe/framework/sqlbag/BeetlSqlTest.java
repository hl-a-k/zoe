package com.zoe.framework.sqlbag;

import com.zoe.framework.sqlbag.DbSqlBag;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caizhicong on 2017/7/7.
 */
public class BeetlSqlTest extends BaseTest{

    @Autowired
    DbSqlBag sqlBag;
    @Test
    public void Test(){

        Map<String, Object> params = new HashMap<>();
        params.put("dateStart", "2015-01-01");
        params.put("dateEnd", "2015-10-10");
        params.put("age", "2");
        params.put("name", "张三");

        String sql = "select * from user where 1=1\n" +
                "@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +


                "@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +"@if(!isEmpty(age)){\n" +
                "and age = #age#\n" +
                "@}\n" +



                "@if(!isEmpty(name)){\n" +
                "and name = #name#\n" +
                "@}";

        long t1 = System.nanoTime();
        String sql2 = sqlBag.get("getInterfaceLog", params);
        long t2 = System.nanoTime();
        System.out.println((t2-t1));
        System.out.println(sql2);

        params.remove("name");
        t1 = System.nanoTime();
        sql2 = sqlBag.get("getInterfaceLog", params);
        t2 = System.nanoTime();
        System.out.println((t2-t1));
    }
}
