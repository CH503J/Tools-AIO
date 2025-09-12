package com.ch503j.workflow.pojo.vo;

import lombok.Data;

import java.util.Date;

@Data
public class HistoryVO {
    private String activityId;
    private String activityName;
    private String activityType;
    private String assignee;
    private Date startTime;
    private Date endTime;

    // 扩展字段
    private String actionType;          // activity/task/comment
    private String comment;             // 批注内容
    private String taskId;              // 任务ID
    private String executionId;         // 执行ID
    private String processDefinitionId; // 流程定义ID
    private String owner;               // 任务所有人（owner）
}