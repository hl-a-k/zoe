package com.zoe.framework.quartz;

import com.zoe.framework.context.SpringContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.*;


/**
 * @author chenjianlin
 * @Description: 计划任务管理
 * @date 2014年4月25日 下午2:43:54
 */
public abstract class AbstractTaskService {
    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    //@Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    public static void main(String[] args) {
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("xxxxx");
    }

    public SchedulerFactoryBean getSchedulerFactoryBean() {
        if (schedulerFactoryBean == null) {
            schedulerFactoryBean = SpringContextHolder.getBean(SchedulerFactoryBean.class);
            if (schedulerFactoryBean == null) {
                WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                if (wac != null) {
                    schedulerFactoryBean = (SchedulerFactoryBean) wac.getBean("schedulerFactoryBean");
                }
                if (schedulerFactoryBean == null) {
                    logger.warn("没有配置bean：SchedulerFactoryBean，将自动创建···");
                    schedulerFactoryBean = new SchedulerFactoryBean();
                }
            }
        }
        return schedulerFactoryBean;
    }

    /**
     * 获取要执行的任务列表
     *
     * @return
     */
    public abstract List<? extends ScheduleJob> getJobs();

    /**
     * 获取任务
     *
     * @param jobId 任务ID
     * @return
     */
    public abstract ScheduleJob getJob(String jobId);

    /**
     * 持久化job
     *
     * @param job 任务对象
     * @return
     */
    public abstract int insertJob(ScheduleJob job);

    /**
     * 更新job
     *
     * @param job 任务对象
     * @return
     */
    public abstract int updateJob(ScheduleJob job);

    /**
     * 创建job
     *
     * @return
     */
    public abstract ScheduleJob createJob();

    /**
     * 添加到数据库中 区别于addJob
     */
    public void addTask(ScheduleJob job) {
        if (StringUtils.isBlank(job.getId())) {
            job.setId(UUID.randomUUID().toString().replace("-", ""));
            insertJob(job);
        } else {
            updateJob(job);
        }
    }

    /**
     * 更改任务状态
     *
     * @throws SchedulerException
     */
    public void changeStatus(String jobId, String cmd) throws SchedulerException {
        ScheduleJob job = getJob(jobId);
        if (job == null) {
            return;
        }
        if ("stop".equals(cmd)) {
            deleteJob(job);
            job.setJobStatus(ScheduleJob.STATUS_NOT_RUNNING);
        } else if ("start".equals(cmd)) {
            job.setJobStatus(ScheduleJob.STATUS_RUNNING);
            addJob(job);
        }
        updateJob(job);
    }

    /**
     * 更改任务 cron表达式
     *
     * @throws SchedulerException
     */
    public void updateCron(String jobId, String cron) throws SchedulerException {
        ScheduleJob job = getJob(jobId);
        if (job == null) {
            return;
        }
        job.setCronExpression(cron);
        if (ScheduleJob.STATUS_RUNNING.equals(job.getJobStatus())) {
            updateJobCron(job);
        }
        updateJob(job);
    }

    /**
     * 添加任务
     *
     * @param job
     * @throws SchedulerException
     */
    public void addJob(ScheduleJob job) throws SchedulerException {
        if (job == null || !ScheduleJob.STATUS_RUNNING.equals(job.getJobStatus())) {
            return;
        }

        Scheduler scheduler = getSchedulerFactoryBean().getScheduler();
        logger.debug(scheduler + ".......................................................................................add");
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        // 不存在，创建一个
        if (null == trigger) {
            Class clazz = ScheduleJob.CONCURRENT_IS.equals(job.getIsConcurrent()) ? QuartzJobFactory.class : QuartzJobFactoryDisallowConcurrentExecution.class;

            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).build();

            jobDetail.getJobDataMap().put("scheduleJob", job);

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

            trigger = TriggerBuilder.newTrigger().
                    withIdentity(job.getJobName(), job.getJobGroup()).
                    startAt(job.getStartTime() == null ? new Date() : job.getStartTime()).
                    endAt(job.getEndTime()).withSchedule(scheduleBuilder).build();

            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            // Trigger已存在，那么更新相应的定时设置
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }

    @PostConstruct
    public void init() throws Exception {
        SchedulerFactoryBean schedulerFactoryBean = getSchedulerFactoryBean();
        if (schedulerFactoryBean != null) {
            // 这里获取任务信息数据
            List<ScheduleJob> jobList = (List<ScheduleJob>) getJobs();

            for (ScheduleJob job : jobList) {
                addJob(job);
            }
        } else {
            System.out.println("Quartz 未配置...");
        }
    }

    /**
     * 获取所有计划中的任务列表
     *
     * @return
     * @throws SchedulerException
     */
    public List<ScheduleJob> getAllJob() throws SchedulerException {
        Scheduler scheduler = getSchedulerFactoryBean().getScheduler();
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
        List<ScheduleJob> jobList = new ArrayList<ScheduleJob>();
        for (JobKey jobKey : jobKeys) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : triggers) {
                ScheduleJob job = createJob();
                job.setJobName(jobKey.getName());
                job.setJobGroup(jobKey.getGroup());
                job.setDescription("触发器:" + trigger.getKey());
                Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                job.setJobStatus(triggerState.name());
                if (trigger instanceof CronTrigger) {
                    CronTrigger cronTrigger = (CronTrigger) trigger;
                    String cronExpression = cronTrigger.getCronExpression();
                    job.setCronExpression(cronExpression);
                }
                jobList.add(job);
            }
        }
        return jobList;
    }

    /**
     * 所有正在运行的job
     *
     * @return
     * @throws SchedulerException
     */
    public List<ScheduleJob> getRunningJob() throws SchedulerException {
        Scheduler scheduler = getSchedulerFactoryBean().getScheduler();
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        List<ScheduleJob> jobList = new ArrayList<>(executingJobs.size());
        for (JobExecutionContext executingJob : executingJobs) {
            ScheduleJob job = createJob();
            JobDetail jobDetail = executingJob.getJobDetail();
            JobKey jobKey = jobDetail.getKey();
            Trigger trigger = executingJob.getTrigger();
            job.setJobName(jobKey.getName());
            job.setJobGroup(jobKey.getGroup());
            job.setDescription("触发器:" + trigger.getKey());
            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
            job.setJobStatus(triggerState.name());
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                String cronExpression = cronTrigger.getCronExpression();
                job.setCronExpression(cronExpression);
            }
            jobList.add(job);
        }
        return jobList;
    }

    /**
     * 暂停一个job
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = getSchedulerFactoryBean().getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复一个job
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void resumeJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = getSchedulerFactoryBean().getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.resumeJob(jobKey);
    }

    /**
     * 删除一个job
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = getSchedulerFactoryBean().getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.deleteJob(jobKey);
    }

    /**
     * 立即执行job
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void runAJobNow(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = getSchedulerFactoryBean().getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.triggerJob(jobKey);
    }

    /**
     * 更新job时间表达式
     *
     * @param scheduleJob
     * @throws SchedulerException
     */
    public void updateJobCron(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = getSchedulerFactoryBean().getScheduler();

        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());

        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();

        scheduler.rescheduleJob(triggerKey, trigger);
    }
}
