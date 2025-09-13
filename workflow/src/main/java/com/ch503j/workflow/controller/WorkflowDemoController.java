package com.ch503j.workflow.controller;

import com.ch503j.common.pojo.dto.BaseResponse;
import com.ch503j.workflow.pojo.dto.TaskDto;
import com.ch503j.workflow.pojo.vo.HistoryVO;
import com.ch503j.workflow.service.WorkflowDemoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
public class WorkflowDemoController {

    @Resource
    private WorkflowDemoService workflowDemoService;

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

        String s = workflowDemoService.startProcess(processKey, businessKey, startUserId);

        return BaseResponse.success("流程实例已开始流转", s);
    }


    /**
     * 查询候选组待办任务（针对候选组）
     *
     * @param userId 用户ID
     * @return 用户可见的待办任务列表
     */
    @GetMapping("/candidate/{userId}")
    public BaseResponse<List<TaskDto>> getCandidateTasks(@PathVariable String userId) {

        return BaseResponse.success(workflowDemoService.getCandidateTasks(userId));
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

        return BaseResponse.success(workflowDemoService.delegateTask(taskId, ownerId, delegateToUserId));
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

        return BaseResponse.success(workflowDemoService.completeTask(taskId, userId, variables, nextAssignee, approve, comment), taskId);
    }

    /**
     * 查询流程实例的历史流转记录
     *
     * @param processInstanceId 流程实例ID
     * @return 历史流转节点列表
     */
    @GetMapping("/history/{processInstanceId}")
    public BaseResponse<List<HistoryVO>> getProcessHistory(@PathVariable String processInstanceId) {

        return BaseResponse.success(workflowDemoService.getProcessHistory(processInstanceId));
    }
}
