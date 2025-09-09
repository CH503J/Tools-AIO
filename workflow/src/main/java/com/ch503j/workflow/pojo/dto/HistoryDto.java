package com.ch503j.workflow.pojo.dto;

import lombok.Data;

@Data
public class HistoryDto {
    private String activityId;
    private String activityName;
    private String activityType;
    private String assignee;
    private java.util.Date startTime;
    private java.util.Date endTime;
}