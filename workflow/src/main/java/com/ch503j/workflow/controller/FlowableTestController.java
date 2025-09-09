package com.ch503j.workflow.controller;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class FlowableTestController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    public FlowableTestController(RuntimeService runtimeService,
                                  TaskService taskService,
                                  HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
    }

    /**
     * 1. 启动流程实例
     */
    @PostMapping("/start")
    public String startProcess(@RequestParam String processKey,
                               @RequestParam(required = false) String businessKey,
                               @RequestBody(required = false) Map<String, Object> variables) {
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, businessKey, variables);
        return "流程已启动，实例ID: " + instance.getId();
    }

    /**
     * 2. 查询任务
     */
    @GetMapping("/tasks")
    public List<Task> getTasks(@RequestParam(required = false) String assignee,
                               @RequestParam(required = false) String candidateGroup,
                               @RequestParam(required = false) String processInstanceId) {
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .taskCandidateGroup(candidateGroup)
                .processInstanceId(processInstanceId)
                .list();
    }

    /**
     * 3. 签收任务
     */
    @PostMapping("/tasks/{taskId}/claim")
    public String claimTask(@PathVariable String taskId, @RequestParam String assignee) {
        taskService.claim(taskId, assignee);
        return "任务 " + taskId + " 已签收给 " + assignee;
    }

    /**
     * 4. 完成任务
     */
    @PostMapping("/tasks/{taskId}/complete")
    public String completeTask(@PathVariable String taskId,
                               @RequestBody(required = false) Map<String, Object> variables) {
        taskService.complete(taskId, variables);
        return "任务 " + taskId + " 已完成";
    }

    /**
     * 5. 查询流程历史（进度）
     */
    @GetMapping("/{instanceId}/history")
    public String getHistory(@PathVariable String instanceId) {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricTaskInstanceEndTime().asc()
                .list();

        StringBuilder sb = new StringBuilder();
        sb.append("流程实例: ").append(instanceId).append("\n");
        if (hpi != null) {
            sb.append("状态: ").append(hpi.getEndTime() == null ? "进行中" : "已结束").append("\n");
        }
        for (HistoricTaskInstance task : tasks) {
            sb.append("任务: ").append(task.getName())
                    .append("ID：").append(task.getId())
                    .append("，受理人: ").append(task.getAssignee())
                    .append("，开始: ").append(task.getStartTime())
                    .append("，结束: ").append(task.getEndTime())
                    .append("\n");
        }
        return sb.toString();
    }
}
