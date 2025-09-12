package com.ch503j.workflow.controller;

import com.ch503j.common.pojo.dto.BaseResponse;
import com.ch503j.workflow.pojo.dto.TaskDto;
import com.ch503j.workflow.pojo.vo.HistoryVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WorkflowController
 * <p>
 * 该控制器提供了基于 Flowable 的工作流操作接口，包括：
 * 1. 启动流程
 * 2. 查询用户待办任务
 * 3. 领取任务
 * 4. 完成任务
 * 5. 退回/释放任务
 * <p>
 * 核心概念说明：
 * - 流程实例(ProcessInstance)：流程引擎中运行的某个流程执行实例，每启动一次流程就会生成一个新的实例。
 * - 流程定义Key(processKey)：流程模型在引擎中的唯一标识，用于启动流程实例。
 * - 业务键(businessKey)：业务系统中用来标识具体业务对象的唯一标识，便于将流程实例和业务对象关联。
 * - 任务(Task)：流程执行过程中产生的具体操作节点，可以被用户领取和完成。
 * - 候选任务(Task Candidate)：任务未被任何人领取前，可以由候选用户或候选组领取。
 * - 领取任务(Claim)：将候选任务分配给具体用户，使其成为任务负责人。
 */

@Slf4j
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

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

    /**
     * 启动流程
     *
     * @param processKey  流程定义的唯一标识Key
     * @param businessKey 业务系统中用于关联业务对象的Key
     * @param startUserId 启动流程的用户ID（在流程中作为发起人）
     * @return 流程实例ID（用于后续查询流程状态或操作任务）
     * <p>
     * 说明：
     * - identityService.setAuthenticatedUserId(startUserId) 用于设置当前操作用户，这个用户将作为流程发起人记录在流程实例中。
     * - runtimeService.startProcessInstanceByKey 会根据流程定义Key创建一个新的流程实例。
     */
    @PostMapping("/start")
    public BaseResponse<String> startProcess(@RequestParam String processKey,
                                             @RequestParam String businessKey,
                                             @RequestParam String startUserId) {
        identityService.setAuthenticatedUserId(startUserId);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey, businessKey);
        return BaseResponse.success("流程实例已开始流转", instance.getId());
    }


    /**
     * 查询候选组待办任务（针对候选组）
     *
     * @param userId 用户ID
     * @return 用户可见的待办任务列表
     */
    @GetMapping("/candidate/{userId}")
    public BaseResponse<List<TaskDto>> getCandidateTasks(@PathVariable String userId) {
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

        return BaseResponse.success(dtoList);
    }


    /**
     * 转办任务（通过在主任务下挂载子任务来实现）
     * Flowable 7.0.0.M2 版本
     *
     * @param taskId           待办任务id
     * @param ownerId          当前委托人id（通常是主管）
     * @param delegateToUserId 被委派人id
     */
    @PostMapping("/delegate")
    public BaseResponse<String> delegateTask(
            @RequestParam String taskId,
            @RequestParam String ownerId,
            @RequestParam String delegateToUserId
    ) {
        // 查询当前任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return BaseResponse.fail("任务不存在或已完成");
        }

        // 设置 owner（委托人）
        taskService.setOwner(taskId, ownerId);

        // 执行委托操作
        taskService.delegateTask(taskId, delegateToUserId);

        // 添加批注，方便历史查询展示“转办痕迹”
        String processInstanceId = task.getProcessInstanceId();
        String message = String.format("任务由 %s 转办给 %s", ownerId, delegateToUserId);
        taskService.addComment(taskId, processInstanceId, message);

        return BaseResponse.success("转办成功");
    }


    /**
     * 提交任务（审批）
     * taskId 和 userId 必传，variables 可选
     * nextAssignee 在研发主管分配和代理主管分配时必传
     * approve 在主管审核稿件时必传
     *
     * @param taskId       待办任务id
     * @param userId       审批人id
     * @param variables    提交时携带的信息（审批人）
     * @param nextAssignee 指定的产出节点经办候选人（研发人、代理人）
     * @param approve      审稿是否通过
     * @param comment      提交时备注
     * @return 提交结果
     */
    @PostMapping("/complete")
    public BaseResponse<String> completeTask(
            @RequestParam String taskId,
            @RequestParam String userId,
            @RequestParam(required = false) Map<String, Object> variables,
            @RequestParam(required = false) String nextAssignee, // 下一节点经办人
            @RequestParam(required = false) Boolean approve,
            @RequestParam(required = false) String comment // 提交备注
    ) {

        // 1. 查询任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return BaseResponse.fail("任务不存在或已完成");
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
                    return BaseResponse.fail("审批任务必须提供 approve 参数");
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

        return BaseResponse.success("任务已提交", taskId);
    }

    /**
     * 查询流程实例的历史流转记录
     *
     * @param processInstanceId 流程实例ID
     * @return 历史流转节点列表
     */
    @GetMapping("/history/{processInstanceId}")
    public BaseResponse<List<HistoryVO>> getProcessHistory(@PathVariable String processInstanceId) {

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

        return BaseResponse.success(dtoList);
    }
}
