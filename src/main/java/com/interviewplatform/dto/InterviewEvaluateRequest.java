package com.interviewplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewEvaluateRequest {

    private Long questionId;

    @NotBlank(message = "Question text is required")
    private String question;

    @NotBlank(message = "Answer is required")
    private String answer;
}
