package com.ch503j.workflow.controller;

import com.ch503j.common.pojo.dto.BaseResponse;
import com.ch503j.workflow.pojo.dto.HistoryDto;
import com.ch503j.workflow.pojo.dto.TaskDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                .taskCandidateUser(userId)   // 用户作为候选人能看到的任务
                .taskUnassigned()            // 未被认领
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
     * 查询经办人待办任务（针对经办人）
     *
     * @param userId 用户ID
     * @return 用户可见的待办任务列表
     */
    @GetMapping("/my-tasks/{userId}")
    public BaseResponse<List<TaskDto>> getMyTasks(@PathVariable String userId) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId)   // 查询已认领的任务
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

    @PostMapping("/complete")
    public BaseResponse<String> completeTask(
            @RequestParam String taskId,
            @RequestParam String userId,
            @RequestParam(required = false) Map<String, Object> variables,
            @RequestParam(required = false) String nextAssignee, // 新增：下一节点经办人
            @RequestParam(required = false) Boolean approve
    ) {

        // 1. 查询任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return BaseResponse.fail("任务不存在或已完成");
        }

        // 2. 获取流程模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        // 3. 校验是否是审批任务
        if (flowElement instanceof UserTask) {
            UserTask userTask = (UserTask) flowElement;
            String isApproveTask = userTask.getAttributeValue("http://flowable.org/bpmn", "isApproveTask");

            if ("true".equalsIgnoreCase(isApproveTask)) {
                // 审核任务必须传递 approve 参数
                if (approve == null) {
                    return BaseResponse.fail("审批任务必须提供 approve 参数");
                }

                if (variables == null) {
                    variables = new HashMap<>();
                }
                variables.put("approve", approve);
            }
        }

        // 1. 如果指定了下一节点 assignee，则先把它放入流程变量
        if (nextAssignee != null && !nextAssignee.isEmpty()) {
            if (variables == null) {
                variables = new HashMap<>();
            }
            // 这里的 key 要和 userTask 的 assignee 表达式一致
            variables.put("assigneeUserId", nextAssignee);
        }


        // 2. 认领任务
        taskService.claim(taskId, userId);

        // 3. 完成任务，携带流程变量
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
    public BaseResponse<List<HistoryDto>> getProcessHistory(@PathVariable String processInstanceId) {
        List<HistoricActivityInstance> historyList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .orderByHistoricActivityInstanceId().asc()
                .list();

        List<HistoryDto> dtoList = historyList.stream().map(h -> {
            HistoryDto dto = new HistoryDto();
            dto.setActivityId(h.getActivityId());
            dto.setActivityName(h.getActivityName());
            dto.setActivityType(h.getActivityType());
            dto.setAssignee(h.getAssignee());
            dto.setStartTime(h.getStartTime());
            dto.setEndTime(h.getEndTime());
            return dto;
        }).collect(Collectors.toList());

        return BaseResponse.success(dtoList);
    }
}
