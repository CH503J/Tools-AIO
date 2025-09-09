package com.ch503j.workflow.controller;

import com.ch503j.common.pojo.dto.BaseResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 批量终止所有流程
 */
@Slf4j
@RestController
@RequestMapping("/processCRUD")
public class ProcessController {

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private RepositoryService repositoryService;

    /**
     * 终止系统中所有在流转的流程实例
     *
     * 说明：
     * - 通过 RuntimeService 查询所有正在运行的流程实例
     * - 调用 deleteProcessInstance 方法逐个终止实例，并可记录原因
     * - 请谨慎调用，该操作不可恢复，会删除所有正在执行的流程实例
     */
    @PostMapping("/terminateAll")
    public BaseResponse<List<Map<String, String>>> terminateAllProcesses(
            @RequestParam(required = false) String reason) {

        List<ProcessInstance> runningInstances = runtimeService.createProcessInstanceQuery().list();

        if (runningInstances.isEmpty()) {
            return BaseResponse.success("没有正在运行的流程实例", List.of());
        }

        String terminationReason = (reason != null && !reason.isEmpty()) ? reason : "系统管理员终止";

        List<Map<String, String>> terminatedInfo = runningInstances.stream()
                .map(pi -> {
                    // 先记录信息
                    Map<String, String> info = Map.of(
                            "id", pi.getId()
                    );
                    // 删除实例
                    runtimeService.deleteProcessInstance(pi.getId(), terminationReason);
                    return info;
                }).toList();

        return BaseResponse.success(terminatedInfo);
    }

    /**
     * 查询所有正在运行的流程实例ID
     *
     * @return 正在运行的流程实例ID列表
     */
    @GetMapping("/instances/running")
    public BaseResponse<List<String>> getRunningProcessInstances() {
        List<ProcessInstance> runningInstances = runtimeService.createProcessInstanceQuery().list();

        if (runningInstances.isEmpty()) {
            return BaseResponse.success("没有正在运行的流程实例", List.of());
        }

        List<String> instanceIds = runningInstances.stream()
                .map(ProcessInstance::getId)
                .toList();

        return BaseResponse.success(instanceIds);
    }


    /**
     * 查询所有流程定义模板
     * @return
     */
    @GetMapping("/process-definitions")
    public List<Map<String, Object>> getAllProcessDefinitions() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()  // 只查询最新版本
                .orderByProcessDefinitionName().asc()
                .list()
                .stream()
                .map(pd -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", pd.getId());                 // 流程定义ID
                    map.put("processId", pd.getKey());        // processId
                    map.put("name", pd.getName());            // 流程名称
                    map.put("version", pd.getVersion());      // 版本号
                    map.put("deploymentId", pd.getDeploymentId());
                    return map;
                })
                .toList();
    }


    /**
     * 根据 processId 删除流程定义及其部署
     * @param processId 流程定义 key（processId）
     */
    @DeleteMapping("/process-definitions/{processId}")
    public String deleteProcessDefinition(@PathVariable String processId) {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processId)
                .list();

        if (processDefinitions.isEmpty()) {
            return "未找到 processId=" + processId + " 的流程定义";
        }

        for (ProcessDefinition pd : processDefinitions) {
            repositoryService.deleteDeployment(pd.getDeploymentId(), true); // true 表示级联删除，包含流程实例
        }

        return "已删除 processId=" + processId + " 的流程定义及相关部署";
    }
}
