package com.zoe.framework.quartz;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by caizhicong on 2016/7/25.
 */
public interface ScheduleJob extends Serializable {

    String STATUS_RUNNING = "1";
    String STATUS_NOT_RUNNING = "0";
    String CONCURRENT_IS = "1";
    String CONCURRENT_NOT = "0";
    /**
     * 获取：主键ID
     *
     * @return 主键ID
     */
    String getId();

    /**
     * 设置：主键ID
     *
     * @param id 主键ID
     */
    void setId(String id);

    /**
     * 获取：任务名称
     *
     * @return 任务名称
     */
    String getJobName();

    /**
     * 设置：任务名称
     *
     * @param jobName 任务名称
     */
    void setJobName(String jobName);

    /**
     * 获取：任务分组
     *
     * @return 任务分组
     */
    String getJobGroup();

    /**
     * 设置：任务分组
     *
     * @param jobGroup 任务分组
     */
    void setJobGroup(String jobGroup);

    /**
     * 获取：任务状态 是否启动任务
     *
     * @return 任务状态 是否启动任务
     */
    String getJobStatus();

    /**
     * 设置：任务状态 是否启动任务
     *
     * @param jobStatus 任务状态 是否启动任务
     */
    void setJobStatus(String jobStatus);

    /**
     * 获取：cron表达式
     *
     * @return cron表达式
     */
    String getCronExpression();

    /**
     * 设置：cron表达式
     *
     * @param cronExpression cron表达式
     */
    void setCronExpression(String cronExpression);

    /**
     * 获取：描述
     *
     * @return 描述
     */
    String getDescription();

    /**
     * 设置：描述
     *
     * @param description 描述
     */
    void setDescription(String description);

    /**
     * 获取：任务执行时调用哪个类的方法 包名+类名
     *
     * @return 任务执行时调用哪个类的方法 包名+类名
     */
    String getBeanClass();

    /**
     * 设置：任务执行时调用哪个类的方法 包名+类名
     *
     * @param beanClass 任务执行时调用哪个类的方法 包名+类名
     */
    void setBeanClass(String beanClass);

    /**
     * 获取：任务是否有状态
     *
     * @return 任务是否有状态
     */
    String getIsConcurrent();

    /**
     * 设置：任务是否有状态
     *
     * @param isConcurrent 任务是否有状态
     */
    void setIsConcurrent(String isConcurrent);

    /**
     * 获取：spring bean
     *
     * @return spring bean
     */
    String getSpringId();

    /**
     * 设置：spring bean
     *
     * @param springId spring bean
     */
    void setSpringId(String springId);

    /**
     * 获取：任务调用的方法名
     *
     * @return 任务调用的方法名
     */
    String getMethodName();

    /**
     * 设置：任务调用的方法名
     *
     * @param methodName 任务调用的方法名
     */
    void setMethodName(String methodName);

    /**
     * 获取：开始时间
     *
     * @return 开始时间
     */
    Date getStartTime();

    /**
     * 设置：开始时间
     *
     * @param startTime 开始时间
     */
    void setStartTime(Date startTime);

    /**
     * 获取：结束时间
     *
     * @return 结束时间
     */
    Date getEndTime();

    /**
     * 设置：结束时间
     *
     * @param endTime 结束时间
     */
    void setEndTime(Date endTime);
}
