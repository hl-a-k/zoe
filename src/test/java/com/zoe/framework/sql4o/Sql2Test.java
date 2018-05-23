package com.zoe.framework.sql4o;

import com.zoe.framework.sql2o.quirks.OracleQuirks;
import com.zoe.framework.sql2o.quirks.ServerType;
import com.zoe.framework.util.SqlFilter;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/6.
 */
public class Sql2Test {

    private static String parseSql(String sql) {
        Map<String, List<Integer>> mapToFill = new HashMap<>();
        OracleQuirks quirks = new OracleQuirks();
        String parsedQuery = quirks.getSqlParameterParsingStrategy().parseSql(sql, mapToFill);
        System.out.println(mapToFill);
        return parsedQuery;
    }

    public static void main(String[] args) {
        //todo 如果打开logback日志，则统计代码执行时间会受到较大影响
        Sql2oSelectBuilder.getTableName(BaseDict.class);
        SqlFilter.filter("test");

        new SelectBuilder();//暖场~一次性

        long t21 = System.nanoTime();
        Sql2oSelectBuilder sqlBuilder = new Sql2oSelectBuilder()
                .setAutoFixField(true)//开启自动匹配字段，性能将有所损失
                .select("a.id", "a.code", "b.dictName as name")
                .from(BaseDict.class, "a")
                .leftJoin(BaseDict.class, "b", "a.id", "b.id")
                .where("a.dictName", "SEX_DICT")
                //.where("--drop database zoeddc;","aaa") value已经使用参事化，可以不用sql注入判断了
                .where("e.salary > 100000")
                .where("a.validFlag", "1")
                .where("b.validFlag", "1")
                .whereLike("dictName", "%")
                .whereLike("dictName", "__")
                .whereLike("dictName", "test")
                .where("validFlag", "1")
                .whereIn("validFlag", "0,1")
                .whereIn("validFlag", "0,1,2,3,4,5,6,7,8,9")
                .whereIn("validFlag", "'0','1'")
                .whereIn("sortNo", "0,1")
                .where("spellCode", "1")
                .groupBy("dictName")
                .having("count(1) > 1")
                .groupBys("a.dictName", "a.dictChsName")
                .orderBys("a.sortNo", "b.sortNo");
        long t22 = System.nanoTime();
        System.out.println("takes ms : " + (t22 - t21) / 1000.0 / 1000.0);
        System.out.println(sqlBuilder);
        System.out.println(sqlBuilder.getParameterMap());
    }

    public static void main3(String[] args) {
        Sql2oSelectBuilder.getTableName(BaseDict.class);

        Long t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11;
        Long first = t1 = System.nanoTime();
        Sql2oSelectBuilder sqlBuilder = new Sql2oSelectBuilder();
        t2 = (System.nanoTime() - first) / 1000;
        long t21 = System.currentTimeMillis();
        t3 = (System.nanoTime() - first) / 1000;
        sqlBuilder.setAutoFixField(false);
        t4 = (System.nanoTime() - first) / 1000;
        sqlBuilder.select("a.id", "a.name", "b.name as deptName");
        t5 = (System.nanoTime() - first) / 1000;
        sqlBuilder.from(BaseDict.class, "a");//100+
        t6 = (System.nanoTime() - first) / 1000;
        sqlBuilder.leftJoin(BaseDict.class, "b", "a.id", "b.id");//40+
        t7 = (System.nanoTime() - first) / 1000;
        sqlBuilder.where("a.name", "SEX_DICT");//2000+
        t8 = (System.nanoTime() - first) / 1000;
        sqlBuilder.where("e.salary > 100000");
        t9 = (System.nanoTime() - first) / 1000;
        sqlBuilder.where("a.validFlag", "1");
        t10 = (System.nanoTime() - first) / 1000;
        sqlBuilder.where("b.validFlag", "1");
        t11 = (System.nanoTime() - first) / 1000;
        long t22 = System.currentTimeMillis();
        System.out.println("takes " + (t22 - t21));
        System.out.println(sqlBuilder);
        System.out.println(sqlBuilder.getParameterMap());
        System.out.println(t1 + "," + t2 + "," + t3 + "," + t4 + "," + t5 + "," + t6 + "," + t7 + "," + t8 + "," + t9 + "," + "," + t10 + "," + t11);
    }

    public static void main1(String[] args) {

        Sql2.getTableName(BaseDict.class);
        SqlFilter.filter("test");

        long t1 = System.currentTimeMillis();
        Sql2 sql2 = Sql2.create().server(ServerType.PostgreSQL)
                .of(BaseDict.class)
                .column("dictName")
                .select()
                .from(BaseDict.class)
                //.select().from("BASE_DICT")
                //.leftJoin(BaseDict.class).on("ID", "ID")
                .where("reportName", "test")
                .whereLike("reportName", "test")
                .where("dictName", "test")
                .whereLike("dictName", "test")
                .where("validFlag", "1")
                .whereIn("validFlag", "0,1")
                .whereIn("validFlag", "'0','1'")
                .whereIn("sortNo", "0,1")
                .where("spellCode", "1")
                .groupBy("dictName")
                .having("count(1) > 1")
                .orderBy("sortNo desc");

        String sql = sql2.getSql();
        long t2 = System.currentTimeMillis();
        System.out.println("build sql takes :" + (t2 - t1));

        System.out.println(sql);
        System.out.println("");
        t1 = System.currentTimeMillis();
        System.out.println(parseSql(sql));
        t2 = System.currentTimeMillis();
        System.out.println("parse sql takes :" + (t2 - t1));
        System.out.println(sql2.getArgs());
    }


    @Entity
    @Table(name = "BASE_DICT")
    public class BaseDict implements java.io.Serializable {

        private static final long serialVersionUID = 1L;

        @Id
        @Column(name = "ID")
        private String id;
        @Column(name = "DICT_NAME")
        private String dictName;
        @Column(name = "DICT_CHS_NAME")
        private String dictChsName;
        @Column(name = "CODE")
        private String code;
        @Column(name = "VALUE")
        private String value;
        @Column(name = "SORT_NO")
        private Integer sortNo;
        @Column(name = "WBZX_CODE")
        private String wbzxCode;
        @Column(name = "SPELL_CODE")
        private String spellCode;
        @Column(name = "VALID_FLAG")
        private String validFlag;
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "CREATE_TIME")
        private Date createTime;
        @Column(name = "CREATE_USER")
        private String createUser;
        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "MODIFY_TIME")
        private Date modifyTime;
        @Column(name = "MODIFY_USER")
        private String modifyUser;
        @Column(name = "REMARK")
        private String remark;

        /**
         * 获取：ID
         *
         * @return ID
         */
        public String getId() {
            return this.id;
        }

        /**
         * 设置：ID
         *
         * @param id ID
         */
        public void setId(String id) {
            this.id = id == null ? null : id.trim();
        }

        /**
         * 获取：字典名称
         *
         * @return 字典名称
         */
        public String getDictName() {
            return this.dictName;
        }

        /**
         * 设置：字典名称
         *
         * @param dictName 字典名称
         */
        public void setDictName(String dictName) {
            this.dictName = dictName == null ? null : dictName.trim();
        }

        /**
         * 获取：字典中文名称
         *
         * @return 字典中文名称
         */
        public String getDictChsName() {
            return this.dictChsName;
        }

        /**
         * 设置：字典名称
         *
         * @param dictChsName 字典中文名称
         */
        public void setDictChsName(String dictChsName) {
            this.dictChsName = dictChsName == null ? null : dictChsName.trim();
        }

        /**
         * 获取：字典代码
         *
         * @return 字典代码
         */
        public String getCode() {
            return this.code;
        }

        /**
         * 设置：字典代码
         *
         * @param code 字典代码
         */
        public void setCode(String code) {
            this.code = code == null ? null : code.trim();
        }

        /**
         * 获取：字典内容
         *
         * @return 字典内容
         */
        public String getValue() {
            return this.value;
        }

        /**
         * 设置：字典内容
         *
         * @param value 字典内容
         */
        public void setValue(String value) {
            this.value = value == null ? null : value.trim();
        }

        /**
         * 获取：排序
         *
         * @return 排序
         */
        public Integer getSortNo() {
            return this.sortNo;
        }

        /**
         * 设置：排序
         *
         * @param sortNo 排序
         */
        public void setSortNo(Integer sortNo) {
            this.sortNo = sortNo;
        }

        /**
         * 获取：五笔码
         *
         * @return 五笔码
         */
        public String getWbzxCode() {
            return this.wbzxCode;
        }

        /**
         * 设置：五笔码
         *
         * @param wbzxCode 五笔码
         */
        public void setWbzxCode(String wbzxCode) {
            this.wbzxCode = wbzxCode == null ? null : wbzxCode.trim();
        }

        /**
         * 获取：拼音码
         *
         * @return 拼音码
         */
        public String getSpellCode() {
            return this.spellCode;
        }

        /**
         * 设置：拼音码
         *
         * @param spellCode 拼音码
         */
        public void setSpellCode(String spellCode) {
            this.spellCode = spellCode == null ? null : spellCode.trim();
        }

        /**
         * 获取：有效标志
         *
         * @return 有效标志
         */
        public String getValidFlag() {
            return this.validFlag;
        }

        /**
         * 设置：有效标志
         *
         * @param validFlag 有效标志
         */
        public void setValidFlag(String validFlag) {
            this.validFlag = validFlag == null ? null : validFlag.trim();
        }

        /**
         * 获取：创建时间
         *
         * @return 创建时间
         */
        public Date getCreateTime() {
            return this.createTime;
        }

        /**
         * 设置：创建时间
         *
         * @param createTime 创建时间
         */
        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        /**
         * 获取：创建人
         *
         * @return 创建人
         */
        public String getCreateUser() {
            return this.createUser;
        }

        /**
         * 设置：创建人
         *
         * @param createUser 创建人
         */
        public void setCreateUser(String createUser) {
            this.createUser = createUser == null ? null : createUser.trim();
        }

        /**
         * 获取：修改时间
         *
         * @return 修改时间
         */
        public Date getModifyTime() {
            return this.modifyTime;
        }

        /**
         * 设置：修改时间
         *
         * @param modifyTime 修改时间
         */
        public void setModifyTime(Date modifyTime) {
            this.modifyTime = modifyTime;
        }

        /**
         * 获取：修改人
         *
         * @return 修改人
         */
        public String getModifyUser() {
            return this.modifyUser;
        }

        /**
         * 设置：修改人
         *
         * @param modifyUser 修改人
         */
        public void setModifyUser(String modifyUser) {
            this.modifyUser = modifyUser == null ? null : modifyUser.trim();
        }

        /**
         * 获取：备注
         *
         * @return 备注
         */
        public String getRemark() {
            return this.remark;
        }

        /**
         * 设置：备注
         *
         * @param remark 备注
         */
        public void setRemark(String remark) {
            this.remark = remark == null ? null : remark.trim();
        }
    }
}
