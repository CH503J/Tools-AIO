package com.ch503j.workflow.pojo.dto;

import lombok.Data;

import java.util.Map;

@Data
public class WorkflowRequestDto {

    /**
     * 流程定义id
     */
    private String processKey;

    /**
     * 业务id
     */
    private String businessKey;

    /**
     * 待办任务id
     */
    private String taskId;

    /**
     * 提交人id
     */
    private String userId;

    /**
     * 产出节点经办候选人（研发人、代理人）
     */
    private String nextAssignee;

    /**
     * 稿件审核结果
     * true: 通过
     * false: 不通过
     */
    private Boolean approve;

    /**
     * 提交备注
     */
    private String comment;

    /**
     * 额外参数
     */
    private Map<String, Object> variables;
}
