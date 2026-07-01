package com.interviewplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionGenerateRequest {

    @NotBlank(message = "Category is required")
    private String category;

    private String difficulty;

    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 20, message = "Count must not exceed 20")
    @Builder.Default
    private Integer count = 5;

    private String resumeContext;
}
