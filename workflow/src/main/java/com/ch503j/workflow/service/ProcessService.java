package com.ch503j.workflow.service;

import java.util.List;
import java.util.Map;

public interface ProcessService {

    List<Map<String, String>> terminateAllProcesses(String reason);

    List<String> getRunningProcessInstances();

    List<Map<String, Object>> getAllProcessDefinitions();

    String deleteProcessDefinition(String processId);
}
