package com.ch503j.workflow.service.impl;

import com.ch503j.common.exception.BusinessException;
import com.ch503j.workflow.service.ProcessService;
import jakarta.annotation.Resource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcessServiceImpl implements ProcessService {

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private RepositoryService repositoryService;

    @Override
    public List<Map<String, String>> terminateAllProcesses(String reason) {

        List<ProcessInstance> runningInstances = runtimeService.createProcessInstanceQuery().list();
        if (runningInstances.isEmpty()) {
            throw new BusinessException("没有正在运行的流程实例");
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

        return terminatedInfo;
    }

    @Override
    public List<String> getRunningProcessInstances() {

        List<ProcessInstance> runningInstances = runtimeService.createProcessInstanceQuery().list();
        if (runningInstances.isEmpty()) {
            throw new BusinessException("没有正在运行的流程实例");
        }

        List<String> instanceIds = runningInstances.stream()
                .map(ProcessInstance::getId)
                .toList();

        return instanceIds;

    }

    @Override
    public List<Map<String, Object>> getAllProcessDefinitions() {
        List<Map<String, Object>> list = repositoryService.createProcessDefinitionQuery()
                .latestVersion()  // 只查询最新版本
                .orderByProcessDefinitionName().asc()
                .list()
                .stream()
                .map(pd -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", pd.getId());                 // 流程定义ID
                    map.put("processId", pd.getKey());         // processId
                    map.put("name", pd.getName());             // 流程名称
                    map.put("version", pd.getVersion());       // 版本号
                    map.put("deploymentId", pd.getDeploymentId());
                    return map;
                })
                .toList();

        return list;
    }

    @Override
    public String deleteProcessDefinition(String processId) {
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
