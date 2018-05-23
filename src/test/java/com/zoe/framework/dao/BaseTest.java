package com.zoe.framework.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by caizhicong on 2016/5/20.
 */
@RunWith(SpringJUnit4ClassRunner.class)  //使用junit4进行测试
@ContextConfiguration({"classpath:beans-config.xml"}) //加载配置文件
public class BaseTest {
    @Test
    public void Test00(){
        //for: initializationError(com.zoe.db.dao.BaseTest): No runnable methods
    }
}
