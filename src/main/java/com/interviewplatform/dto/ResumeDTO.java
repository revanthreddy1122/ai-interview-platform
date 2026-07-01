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
public class ResumeDTO {

    private Long resumeId;
    private String fileName;
    private String resumeText;
    private Long fileSize;
    private LocalDateTime uploadDate;
    private ResumeAnalysisDTO analysis;
}
