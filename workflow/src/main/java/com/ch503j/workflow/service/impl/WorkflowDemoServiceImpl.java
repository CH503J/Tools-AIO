package com.ch503j.workflow.service.impl;

import com.ch503j.common.exception.BusinessException;
import com.ch503j.workflow.pojo.dto.TaskDto;
import com.ch503j.workflow.pojo.vo.HistoryVO;
import com.ch503j.workflow.service.WorkflowDemoService;
import jakarta.annotation.Resource;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowDemoServiceImpl implements WorkflowDemoService {

    @Resource
    private RuntimeService runtimeService; // 流程运行服务，用于启动流程实例、查询流程状态等

    @Resource
    private TaskService taskService; // 任务服务，用于查询、领取、完成任务

    @Resource
    private IdentityService identityService; // 身份服务，用于设置操作用户等

    @Resource
    private HistoryService historyService; // 历史服务，用于查询流程历史数据

    @Resource
    private RepositoryService repositoryService;


    @Override
    public String startProcess(String processKey, String businessKey, String startUserId) {
        identityService.setAuthenticatedUserId(startUserId);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, businessKey);
        return instance.getId();
    }

    @Override
    public List<TaskDto> getCandidateTasks(String userId) {
        List<Task> tasks = taskService.createTaskQuery()
                .or()
                .taskAssignee(userId)        // 已认领的任务（分配给我）
                .taskCandidateUser(userId)   // 未认领的候选任务
                .endOr()
                .orderByTaskCreateTime().asc()
                .list();

        List<TaskDto> dtoList = tasks.stream().map(t -> {
            TaskDto dto = new TaskDto();
            dto.setTaskId(t.getId());
            dto.setTaskName(t.getName());
            dto.setAssignee(t.getAssignee());
            dto.setCreateTime(t.getCreateTime());
            dto.setProcessInstanceId(t.getProcessInstanceId());
            dto.setProcessDefinitionId(t.getProcessDefinitionId());

            // 查 businessKey
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (pi != null) {
                dto.setBusinessKey(pi.getBusinessKey());
            }
            return dto;
        }).collect(Collectors.toList());

        return dtoList;
    }

    @Override
    public String delegateTask(String taskId, String ownerId, String delegateToUserId) {
        // 查询当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已完成");
        }

        // 设置 owner（委托人）
        taskService.setOwner(taskId, ownerId);

        // 执行委托操作
        taskService.delegateTask(taskId, delegateToUserId);

        // 添加批注，方便历史查询展示“转办痕迹”
        String processInstanceId = task.getProcessInstanceId();
        String message = String.format("任务由 %s 转办给 %s", ownerId, delegateToUserId);
        taskService.addComment(taskId, processInstanceId, message);

        return "转办成功";
    }

    @Override
    public String completeTask(String taskId, String userId, Map<String, Object> variables, String nextAssignee, Boolean approve, String comment) {
        // 1. 查询任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已完成");
        }

        // 2. 如果有批注，先加批注
        if (comment != null && !comment.isEmpty()) {
            taskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }

        // 3. 将委托任务转换为普通任务（如果是委托任务）
        if (task.getDelegationState() != null && task.getDelegationState() == DelegationState.PENDING) {
            // 清空 owner
            task.setOwner(null);
            // 清空委托状态，使其变为普通任务
            task.setDelegationState(null);
            taskService.saveTask(task);
        }

        // 4. 获取流程模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        // 5. 审核任务逻辑
        if (flowElement instanceof UserTask) {
            UserTask userTask = (UserTask) flowElement;
            String isApproveTask = userTask.getAttributeValue("http://flowable.org/bpmn", "isApproveTask");

            if ("true".equalsIgnoreCase(isApproveTask)) {
                if (approve == null) {
                    throw new BusinessException("审批任务必须提供 approve 参数");
                }
                if (variables == null) {
                    variables = new HashMap<>();
                }
                variables.put("approve", approve);
            }
        }

        // 6. 指定下一节点 assignee
        if (nextAssignee != null && !nextAssignee.isEmpty()) {
            if (variables == null) {
                variables = new HashMap<>();
            }
            variables.put("assigneeUserId", nextAssignee);
        }

        // 7. 认领任务
        taskService.claim(taskId, userId);

        // 8. 完成任务
        if (variables != null && !variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }
        return "任务已提交";
    }

    @Override
    public List<HistoryVO> getProcessHistory(String processInstanceId) {
        List<HistoryVO> dtoList = new ArrayList<>();

        // === 活动历史（节点执行） ===
        List<HistoricActivityInstance> activityList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        for (HistoricActivityInstance h : activityList) {
            HistoryVO dto = new HistoryVO();
            dto.setActivityId(h.getActivityId());
            dto.setActivityName(h.getActivityName());
            dto.setActivityType(h.getActivityType());
            dto.setAssignee(h.getAssignee());
            dto.setStartTime(h.getStartTime());
            dto.setEndTime(h.getEndTime());
            dto.setActionType("activity");
            dto.setTaskId(h.getTaskId());
            dto.setExecutionId(h.getExecutionId());
            dto.setProcessDefinitionId(h.getProcessDefinitionId());
            dtoList.add(dto);
        }

        // === 任务历史（任务维度，能看到转办/完成等） ===
        List<HistoricTaskInstance> taskList = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();

        for (HistoricTaskInstance t : taskList) {
            HistoryVO dto = new HistoryVO();
            dto.setActivityId(t.getTaskDefinitionKey());
            dto.setActivityName(t.getName());
            dto.setActivityType("userTask");
            dto.setAssignee(t.getAssignee());
            dto.setStartTime(t.getStartTime());
            dto.setEndTime(t.getEndTime());
            dto.setActionType("task");
            dto.setTaskId(t.getId());
            dto.setExecutionId(t.getExecutionId());
            dto.setProcessDefinitionId(t.getProcessDefinitionId());
            dto.setOwner(t.getOwner());
            dtoList.add(dto);
        }

        // === 批注历史（转办/委派/审批意见等） ===
        List<Comment> commentList = taskService.getProcessInstanceComments(processInstanceId);
        for (Comment c : commentList) {
            HistoryVO dto = new HistoryVO();
            dto.setActivityId(c.getTaskId());
            dto.setActivityName("批注");
            dto.setActivityType("comment");
            dto.setAssignee(c.getUserId());
            dto.setStartTime(c.getTime());
            dto.setEndTime(null);
            dto.setActionType("comment");
            dto.setTaskId(c.getTaskId());
            dto.setExecutionId(null); // 批注没有 executionId
            dto.setProcessDefinitionId(null);
            dto.setComment(c.getFullMessage());
            dtoList.add(dto);
        }

        // === 统一排序（按时间线） ===
        dtoList.sort(Comparator.comparing(
                HistoryVO::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));
        return dtoList;
    }
}
