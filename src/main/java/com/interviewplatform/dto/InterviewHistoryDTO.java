package com.interviewplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewHistoryDTO {

    private Long historyId;
    private String questionText;
    private String answerText;
    private Integer score;
    private Integer correctnessScore;
    private Integer completenessScore;
    private Integer communicationScore;
    private String feedback;
    private String suggestions;
    private LocalDateTime attemptedAt;
}
