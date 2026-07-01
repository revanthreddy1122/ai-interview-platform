package com.interviewplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDescriptionMatchRequest {

    @NotNull(message = "Resume id is required")
    private Long resumeId;

    @NotBlank(message = "Job description is required")
    private String jobDescription;
}
