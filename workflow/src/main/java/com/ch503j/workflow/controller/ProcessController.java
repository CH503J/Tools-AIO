package com.ch503j.workflow.controller;

import com.ch503j.common.pojo.dto.BaseResponse;
import com.ch503j.workflow.service.ProcessService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.springframework.web.bind.annotation.*;

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
    private ProcessService processService;

    /**
     * 终止系统中所有在流转的流程实例
     * <p>
     * 说明：
     * - 通过 RuntimeService 查询所有正在运行的流程实例
     * - 调用 deleteProcessInstance 方法逐个终止实例，并可记录原因
     * - 请谨慎调用，该操作不可恢复，会删除所有正在执行的流程实例
     */
    @PostMapping("/terminateAll")
    public BaseResponse<List<Map<String, String>>> terminateAllProcesses(@RequestParam(required = false) String reason) {

        return BaseResponse.success(processService.terminateAllProcesses(reason));
    }

    /**
     * 查询所有正在运行的流程实例ID
     *
     * @return 正在运行的流程实例ID列表
     */
    @GetMapping("/instances/running")
    public BaseResponse<List<String>> getRunningProcessInstances() {

        return BaseResponse.success(processService.getRunningProcessInstances());
    }


    /**
     * 查询所有流程定义模板
     *
     * @return
     */
    @GetMapping("/process-definitions")
    public BaseResponse<List<Map<String, Object>>> getAllProcessDefinitions() {

        return BaseResponse.success(processService.getAllProcessDefinitions());
    }


    /**
     * 根据 processId 删除流程定义及其部署
     *
     * @param processId 流程定义 key（processId）
     */
    @DeleteMapping("/process-definitions/{processId}")
    public BaseResponse<String> deleteProcessDefinition(@PathVariable String processId) {

        return BaseResponse.success(processService.deleteProcessDefinition(processId));
    }
}
