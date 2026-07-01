package com.interviewplatform.controller;

import com.interviewplatform.dto.ApiResponse;
import com.interviewplatform.dto.InterviewEvaluateRequest;
import com.interviewplatform.dto.InterviewEvaluationResponse;
import com.interviewplatform.dto.InterviewHistoryDTO;
import com.interviewplatform.dto.QuestionDTO;
import com.interviewplatform.dto.QuestionGenerateRequest;
import com.interviewplatform.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
@Tag(name = "Interview", description = "AI question generation, mock interview evaluation, and history endpoints")
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping("/questions")
    @Operation(summary = "Generate AI-powered interview questions for a category")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> generateQuestions(
            @Valid @RequestBody QuestionGenerateRequest request) {
        List<QuestionDTO> questions = interviewService.generateQuestions(request);
        return new ResponseEntity<>(ApiResponse.success("Questions generated successfully", questions), HttpStatus.CREATED);
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Evaluate a candidate's answer to an interview question using AI")
    public ResponseEntity<ApiResponse<InterviewEvaluationResponse>> evaluateAnswer(
            @Valid @RequestBody InterviewEvaluateRequest request) {
        InterviewEvaluationResponse response = interviewService.evaluateAnswer(request);
        return ResponseEntity.ok(ApiResponse.success("Answer evaluated successfully", response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get the mock interview history for the current user")
    public ResponseEntity<ApiResponse<List<InterviewHistoryDTO>>> getInterviewHistory() {
        List<InterviewHistoryDTO> history = interviewService.getInterviewHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/questions/history")
    @Operation(summary = "Get the generated question history for the current user")
    public ResponseEntity<ApiResponse<List<QuestionDTO>>> getQuestionHistory() {
        List<QuestionDTO> questions = interviewService.getQuestionHistory();
        return ResponseEntity.ok(ApiResponse.success(questions));
    }
}
