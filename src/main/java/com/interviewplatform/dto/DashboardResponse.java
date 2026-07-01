package com.interviewplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Integer latestAtsScore;
    private Integer resumeCount;
    private Double averageInterviewScore;
    private Integer totalInterviewsAttempted;
    private List<String> strongSkills;
    private List<String> weakSkills;
    private List<InterviewHistoryDTO> recentInterviews;
    private List<ResumeDTO> recentResumes;
}
