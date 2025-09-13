package com.ch503j.workflow.service;

import com.ch503j.workflow.pojo.dto.TaskDto;
import com.ch503j.workflow.pojo.vo.HistoryVO;

import java.util.List;
import java.util.Map;

public interface WorkflowDemoService {

    String startProcess(String processKey, String businessKey, String startUserId);

    List<TaskDto> getCandidateTasks(String userId);

    String delegateTask(String taskId, String ownerId, String delegateToUserId);

    String completeTask(String taskId, String userId, Map<String, Object> variables, String nextAssignee, Boolean approve, String comment);

    List<HistoryVO> getProcessHistory(String processInstanceId);
}
