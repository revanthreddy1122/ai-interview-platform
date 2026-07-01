package com.interviewplatform.controller;

import com.interviewplatform.dto.ApiResponse;
import com.interviewplatform.dto.JobDescriptionMatchRequest;
import com.interviewplatform.dto.JobMatchResponse;
import com.interviewplatform.dto.ResumeAnalysisDTO;
import com.interviewplatform.dto.ResumeDTO;
import com.interviewplatform.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "Resume upload, AI analysis, and job description matching endpoints")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a PDF resume and extract its text content")
    public ResponseEntity<ApiResponse<ResumeDTO>> uploadResume(@RequestParam("file") MultipartFile file) {
        ResumeDTO resumeDTO = resumeService.uploadResume(file);
        return new ResponseEntity<>(ApiResponse.success("Resume uploaded successfully", resumeDTO), HttpStatus.CREATED);
    }

    @PostMapping("/analyze/{resumeId}")
    @Operation(summary = "Run AI-powered ATS analysis on an uploaded resume")
    public ResponseEntity<ApiResponse<ResumeAnalysisDTO>> analyzeResume(@PathVariable Long resumeId) {
        ResumeAnalysisDTO analysisDTO = resumeService.analyzeResume(resumeId);
        return ResponseEntity.ok(ApiResponse.success("Resume analyzed successfully", analysisDTO));
    }

    @PostMapping("/match")
    @Operation(summary = "Match a resume against a job description using AI")
    public ResponseEntity<ApiResponse<JobMatchResponse>> matchJobDescription(
            @Valid @RequestBody JobDescriptionMatchRequest request) {
        JobMatchResponse matchResponse = resumeService.matchJobDescription(request);
        return ResponseEntity.ok(ApiResponse.success("Job description match completed", matchResponse));
    }

    @GetMapping("/history")
    @Operation(summary = "Get the resume upload history for the current user")
    public ResponseEntity<ApiResponse<List<ResumeDTO>>> getResumeHistory() {
        List<ResumeDTO> history = resumeService.getResumeHistory();
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Get a specific resume by id")
    public ResponseEntity<ApiResponse<ResumeDTO>> getResumeById(@PathVariable Long resumeId) {
        ResumeDTO resumeDTO = resumeService.getResumeById(resumeId);
        return ResponseEntity.ok(ApiResponse.success(resumeDTO));
    }
}
