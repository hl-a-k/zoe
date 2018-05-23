package com.zoe.framework.sql4o;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Map;

public class SelectBuilderTest extends TestCase {

    public void testBasics() {
        //
        // Simple tables
        //

        SelectBuilder sb = new SelectBuilder("Employee");
        assertEquals("select * from Employee", sb.toString());

        sb = new SelectBuilder("Employee e");
        assertEquals("select * from Employee e", sb.toString());

        sb = new SelectBuilder("Employee e").column("name");
        assertEquals("select name from Employee e", sb.toString());

        sb = new SelectBuilder("Employee e").column("name").column("age");
        assertEquals("select name, age from Employee e", sb.toString());

        sb = new SelectBuilder("Employee e").column("name as n").column("age");
        assertEquals("select name as n, age from Employee e", sb.toString());

        //
        // Where clauses
        //

        sb = new SelectBuilder("Employee e").where("name like 'Bob%'");
        assertEquals("select * from Employee e where name like 'Bob%'", sb.toString());

        sb = new SelectBuilder("Employee e").where("name like 'Bob%'").where("age > 37");
        assertEquals("select * from Employee e where name like 'Bob%' and age > 37", sb.toString());

        //
        // Join clauses
        //

        sb = new SelectBuilder("Employee e").join("Department d on e.dept_id = d.id");
        assertEquals("select * from Employee e join Department d on e.dept_id = d.id", sb.toString());

        sb = new SelectBuilder("Employee e").join("Department d on e.dept_id = d.id").where("name like 'Bob%'");
        assertEquals("select * from Employee e join Department d on e.dept_id = d.id where name like 'Bob%'", sb
                .toString());

        //
        // Order by clauses
        //

        sb = new SelectBuilder("Employee e").orderBy("name");
        assertEquals("select * from Employee e order by name", sb.toString());

        sb = new SelectBuilder("Employee e").orderBy("name desc").orderBy("age");
        assertEquals("select * from Employee e order by name desc, age", sb.toString());

        sb = new SelectBuilder("Employee").where("name like 'Bob%'").orderBy("age");
        assertEquals("select * from Employee where name like 'Bob%' order by age", sb.toString());

        //
        // For Update
        //

        sb = new SelectBuilder("Employee").where("id = 42").forUpdate();
        assertEquals("select * from Employee where id = 42 for update", sb.toString());

    }

    public void testUnions() {
        SelectBuilder sb = new SelectBuilder()
        .column("a")
        .column("b")
        .from("Foo")
        .where("a > 10")
        .orderBy("1");

        sb.union(new SelectBuilder()
        .column("c")
        .column("d")
        .from("Bar"));

        assertEquals("select a, b from Foo where a > 10 union select c, d from Bar order by 1", sb.toString());
    }

    public void testWhereIn() {
        SelectBuilder sc = new SelectBuilder()
                .column("*")
                .from("Emp")
                .whereIn("name", Arrays.asList("Larry", "Curly", "Moe"));

        assertEquals("select * from Emp where name in (:name0, :name1, :name2)", sc.toString());

        Map<String, Object> map = sc.getParameterMap();

        assertEquals("Larry", map.get("name0"));
        assertEquals("Curly", map.get("name1"));
        assertEquals("Moe", map.get("name2"));
    }
}
