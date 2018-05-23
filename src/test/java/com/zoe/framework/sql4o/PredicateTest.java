package com.zoe.framework.sql4o;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zoe.framework.sql4o.Predicates.*;

public class PredicateTest extends TestCase {

    public void testEq() {
        SelectBuilder sc = new SelectBuilder()
                .column("*")
                .from("Emp")
                .where(eq("name", "Bob"));

        assertEquals("select * from Emp where name = :param0", sc.toString());
        assertEquals("Bob", sc.getParameterMap().get("param0"));
    }

    public void testExists() {
        SelectBuilder sc = new SelectBuilder()
                .column("*")
                .from("Emp e")
                .where(Predicates.exists("SickDay sd").where("sd.emp_id = e.id").and(eq("sd.dow", "Monday")));

        assertEquals("select * from Emp e where exists (select 1 from SickDay sd where sd.emp_id = e.id and sd.dow = :param0)", sc.toString());
        assertEquals("Monday", sc.getParameterMap().get("param0"));
    }

    public void testInArray() {
        SelectBuilder sc = new SelectBuilder()
                .column("*")
                .from("Emp")
                .where(in("name", "Larry", "Curly", "Moe"));

        assertEquals("select * from Emp where name in (:param0, :param1, :param2)", sc.toString());


        Map<String, Object> map = sc.getParameterMap();

        assertEquals("Larry", map.get("param0"));
        assertEquals("Curly", map.get("param1"));
        assertEquals("Moe", map.get("param2"));
    }

    public void testInList() {
        List<String> names = new ArrayList<String>();
        names.add("Larry");
        names.add("Curly");
        names.add("Moe");

        SelectBuilder sc = new SelectBuilder()
                .column("*")
                .from("Emp")
                .where(in("name", names));

        assertEquals("select * from Emp where name in (:param0, :param1, :param2)", sc.toString());

        Map<String, Object> map = sc.getParameterMap();

        assertEquals("Larry", map.get("param0"));
        assertEquals("Curly", map.get("param1"));
        assertEquals("Moe", map.get("param2"));
    }

    public void testNot() {
        SelectBuilder sc = new SelectBuilder()
                .column("*")
                .from("Emp")
                .where(not(eq("name", "Bob")));

        assertEquals("select * from Emp where not (name = :param0)", sc.toString());
        assertEquals("Bob", sc.getParameterMap().get("param0"));
    }

    public void testAnd() {
        SelectBuilder sc = SelectBuilder.newInstance();
        sc.column("*").from("Emp").where(Predicates.and(Predicates.eq("name", "sam"), Predicates.or(Predicates.eq("name", "tony"), Predicates.eq("name", "bob"))));
        assertEquals("select * from Emp where (name = :param0 and (name = :param1 or name = :param2))", sc.toString());
        assertEquals("sam", sc.getParameterMap().get("param0"));
    }

    public void testAnd2() {
        Sql2oSelectBuilder sc = new Sql2oSelectBuilder();
        sc.column("*").from("Emp").where(Predicates.and(Predicates.eq("name", "sam"), Predicates.or(Predicates.eq("name", "tony"), Predicates.eq("name", "bob"))));
        assertEquals("select * from Emp where (name = :param0 and (name = :param1 or name = :param2))", sc.getBuilder().toString());
        assertEquals("sam", sc.getParameterMap().get("param0"));
    }
}
