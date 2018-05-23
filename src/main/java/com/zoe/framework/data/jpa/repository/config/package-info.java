/**
 * 来自 spring-data-jpa ，版本为：1.11.4.RELEASE
 * copy from : org.springframework.data.jpa.repository.config
 * 修改的地方有：
 * 1、去除 entityManager 、 mappingContext 的实现
 * 2、出去 org.springframework.orm.jpa.SharedEntityManagerCreator
 * 3、使用自行实现的Sql2oRepositoryFactoryBean
 *
 * Created by caizhicong on 2017/7/6.
 */
package com.zoe.framework.data.jpa.repository.config;