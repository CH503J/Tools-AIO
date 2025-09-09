package com.ch503j.workflow.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 * TaskDto
 *
 * 用于返回未被认领任务或已分配任务的信息
 */
@Data
public class TaskDto {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称（对应流程节点名）
     */
    private String taskName;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
     */
    private String processDefinitionId;

    /**
     * 任务负责人（未认领时为null）
     */
    private String assignee;

    /**
     * 任务创建时间
     */
    private Date createTime;

    /**
     * 业务主键（方便和业务系统关联）
     */
    private String businessKey;
}
