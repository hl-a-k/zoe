package com.zoe.framework.dao;

import com.alibaba.druid.pool.DruidDataSource;
import com.dangdang.ddframe.rdb.sharding.api.ShardingDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.zoe.framework.dao.algorithm.SingleKeyModuloDatabaseShardingAlgorithm;
import com.zoe.framework.dao.algorithm.SingleKeyModuloTableShardingAlgorithm;
import com.zoe.framework.data.jpa.repository.support.Sql2oCrudService;
import com.zoe.framework.sql2o.Connection;
import com.zoe.framework.sql2o.Sql2o;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by caizhicong on 2016/10/21.
 */
public class ShardingTest extends BaseTest {

    @Autowired
    Sql2o sql2o;

    private static DataSource getShardingDataSource() {
        DataSourceRule dataSourceRule = new DataSourceRule(createDataSourceMap());
        TableRule orderTableRule = TableRule.builder("t_order").actualTables(Arrays.asList("t_order_0", "t_order_1")).dataSourceRule(dataSourceRule).build();
        TableRule orderItemTableRule = TableRule.builder("t_order_item").actualTables(Arrays.asList("t_order_item_0", "t_order_item_1")).dataSourceRule(dataSourceRule).build();
        ShardingRule shardingRule = ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Arrays.asList(orderTableRule, orderItemTableRule))
                .bindingTableRules(Collections.singletonList(new BindingTableRule(Arrays.asList(orderTableRule, orderItemTableRule))))
                .databaseShardingStrategy(new DatabaseShardingStrategy("user_id", new SingleKeyModuloDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy("order_id", new SingleKeyModuloTableShardingAlgorithm())).build();
        try {
            return ShardingDataSourceFactory.createDataSource(shardingRule);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }

    private static DataSource createDataSource(final String dataSourceName) {
        DruidDataSource result = new DruidDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("admin");
        return result;
    }

    //@Test
    public void Test01() {
        try (Connection connection = sql2o.open()) {
            long t1 = System.currentTimeMillis();
            for (int i = 1; i < 100; i++) {
                LogModel logModel = new LogModel();
                logModel.setLogId(i);
                logModel.setLogTitle("log" + i);
                logModel.setLogContent("log_content" + i);
                logModel.setLogTime(new Date());
                Sql2oCrudService.of(connection).insert(logModel);
            }
            long t2 = System.currentTimeMillis();
            System.out.println((t2 - t1));
        }
    }

    //@Test
    public void Test02() {
        try (Connection connection = sql2o.open()) {
            long t1 = System.currentTimeMillis();
            List<LogModel> logModels = Sql2oCrudService.of(connection).findAll(LogModel.class);
            Assert.assertEquals(198, logModels.size());
            long t2 = System.currentTimeMillis();
            System.out.println((t2 - t1));
        }
    }

    //@Test
    public void Test03() {
        try (Connection connection = sql2o.open()) {
            long t1 = System.currentTimeMillis();
            Sql2oCrudService.of(connection).deleteById(LogModel.class,1);
            LogModel logModel = Sql2oCrudService.of(connection).getById(LogModel.class,1);
            Assert.assertNull(logModel);
            long t2 = System.currentTimeMillis();
            System.out.println((t2 - t1));
        }
    }

    @Test
    public void Test04(){
        int value = ((int)"ABC".charAt(0)) % 2;
        System.out.println(value);
    }
}
