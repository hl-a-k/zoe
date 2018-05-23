package com.zoe.framework.dao;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by caizhicong on 2016/10/23.
 */
@Table(name = "log")
public class LogModel {
    @Id
    @Column(name = "log_id")
    private Integer logId;
    @Column(name = "log_title")
    private String logTitle;
    @Column(name = "log_content")
    private String logContent;
    @Column(name = "log_time")
    private Date logTime;

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public String getLogTitle() {
        return logTitle;
    }

    public void setLogTitle(String logTitle) {
        this.logTitle = logTitle;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }
}
