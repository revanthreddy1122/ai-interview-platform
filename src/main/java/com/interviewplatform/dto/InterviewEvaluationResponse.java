package com.interviewplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewEvaluationResponse {

    private Long historyId;
    private Integer score;
    private Integer correctnessScore;
    private Integer completenessScore;
    private Integer communicationScore;
    private String feedback;
    private String suggestions;
}
