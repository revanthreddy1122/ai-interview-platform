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
public class JobMatchResponse {

    private Long resumeId;
    private Integer matchPercentage;
    private List<String> missingSkills;
    private List<String> suggestions;
}
