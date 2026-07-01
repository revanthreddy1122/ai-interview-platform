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
public class QuestionDTO {

    private Long questionId;
    private String questionText;
    private String category;
    private String difficulty;
    private LocalDateTime createdAt;
}
