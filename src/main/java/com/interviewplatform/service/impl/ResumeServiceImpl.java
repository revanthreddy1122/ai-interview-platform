package com.interviewplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.interviewplatform.dto.JobDescriptionMatchRequest;
import com.interviewplatform.dto.JobMatchResponse;
import com.interviewplatform.dto.ResumeAnalysisDTO;
import com.interviewplatform.dto.ResumeDTO;
import com.interviewplatform.entity.DashboardMetrics;
import com.interviewplatform.entity.Resume;
import com.interviewplatform.entity.ResumeAnalysis;
import com.interviewplatform.entity.User;
import com.interviewplatform.exception.ResourceNotFoundException;
import com.interviewplatform.repository.DashboardMetricsRepository;
import com.interviewplatform.repository.ResumeAnalysisRepository;
import com.interviewplatform.repository.ResumeRepository;
import com.interviewplatform.service.AIService;
import com.interviewplatform.service.ResumeService;
import com.interviewplatform.util.JsonParserUtil;
import com.interviewplatform.util.PdfExtractorUtil;
import com.interviewplatform.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final DashboardMetricsRepository dashboardMetricsRepository;
    private final PdfExtractorUtil pdfExtractorUtil;
    private final AIService aiService;
    private final JsonParserUtil jsonParserUtil;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public ResumeDTO uploadResume(MultipartFile file) {
        User currentUser = securityUtil.getCurrentUser();
        String extractedText = pdfExtractorUtil.extractText(file);

        Resume resume = Resume.builder()
                .user(currentUser)
                .fileName(file.getOriginalFilename())
                .resumeText(extractedText)
                .fileSize(file.getSize())
                .build();

        Resume savedResume = resumeRepository.save(resume);

        updateResumeCountMetric(currentUser.getUserId());

        log.info("Resume uploaded for user {}: {}", currentUser.getUserId(), savedResume.getFileName());

        return mapToDTO(savedResume);
    }

    @Override
    @Transactional
    public ResumeAnalysisDTO analyzeResume(Long resumeId) {
        Long userId = securityUtil.getCurrentUserId();
        Resume resume = resumeRepository.findByResumeIdAndUser_UserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));

        JsonNode aiResult = aiService.analyzeResume(resume.getResumeText());

        int atsScore = jsonParserUtil.safeGetInt(aiResult, "atsScore", 0);
        List<String> strengths = jsonParserUtil.nodeToStringList(aiResult.get("strengths"));
        List<String> weaknesses = jsonParserUtil.nodeToStringList(aiResult.get("weaknesses"));
        List<String> missingSkills = jsonParserUtil.nodeToStringList(aiResult.get("missingSkills"));
        List<String> suggestions = jsonParserUtil.nodeToStringList(aiResult.get("suggestions"));

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResume_ResumeId(resumeId)
                .orElse(ResumeAnalysis.builder().resume(resume).build());

        analysis.setAtsScore(atsScore);
        analysis.setStrengths(String.join("|||", strengths));
        analysis.setWeaknesses(String.join("|||", weaknesses));
        analysis.setMissingSkills(String.join("|||", missingSkills));
        analysis.setSuggestions(String.join("|||", suggestions));
        analysis.setAnalyzedAt(LocalDateTime.now());

        ResumeAnalysis savedAnalysis = resumeAnalysisRepository.save(analysis);

        updateAtsScoreMetric(userId, atsScore, strengths, weaknesses);

        log.info("Resume {} analyzed for user {} with ATS score {}", resumeId, userId, atsScore);

        return ResumeAnalysisDTO.builder()
                .analysisId(savedAnalysis.getAnalysisId())
                .resumeId(resumeId)
                .atsScore(atsScore)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .missingSkills(missingSkills)
                .suggestions(suggestions)
                .analyzedAt(savedAnalysis.getAnalyzedAt())
                .build();
    }

    @Override
    @Transactional
    public JobMatchResponse matchJobDescription(JobDescriptionMatchRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        Resume resume = resumeRepository.findByResumeIdAndUser_UserId(request.getResumeId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", request.getResumeId()));

        JsonNode aiResult = aiService.matchJobDescription(resume.getResumeText(), request.getJobDescription());

        int matchPercentage = jsonParserUtil.safeGetInt(aiResult, "matchPercentage", 0);
        List<String> missingSkills = jsonParserUtil.nodeToStringList(aiResult.get("missingSkills"));
        List<String> suggestions = jsonParserUtil.nodeToStringList(aiResult.get("suggestions"));

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResume_ResumeId(request.getResumeId())
                .orElse(ResumeAnalysis.builder().resume(resume).build());

        analysis.setJobDescription(request.getJobDescription());
        analysis.setMatchPercentage(matchPercentage);
        if (analysis.getMissingSkills() == null) {
            analysis.setMissingSkills(String.join("|||", missingSkills));
        }
        if (analysis.getSuggestions() == null) {
            analysis.setSuggestions(String.join("|||", suggestions));
        }
        analysis.setAnalyzedAt(LocalDateTime.now());
        resumeAnalysisRepository.save(analysis);

        log.info("Job description match completed for resume {} with match {}%", request.getResumeId(), matchPercentage);

        return JobMatchResponse.builder()
                .resumeId(request.getResumeId())
                .matchPercentage(matchPercentage)
                .missingSkills(missingSkills)
                .suggestions(suggestions)
                .build();
    }

    @Override
    public List<ResumeDTO> getResumeHistory() {
        Long userId = securityUtil.getCurrentUserId();
        return resumeRepository.findByUser_UserIdOrderByUploadDateDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResumeDTO getResumeById(Long resumeId) {
        Long userId = securityUtil.getCurrentUserId();
        Resume resume = resumeRepository.findByResumeIdAndUser_UserId(resumeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume", "id", resumeId));
        return mapToDTO(resume);
    }

    private void updateResumeCountMetric(Long userId) {
        DashboardMetrics metrics = dashboardMetricsRepository.findByUser_UserId(userId)
                .orElseGet(() -> DashboardMetrics.builder().build());
        long count = resumeRepository.countByUser_UserId(userId);
        metrics.setResumeCount((int) count);
        dashboardMetricsRepository.save(metrics);
    }

    private void updateAtsScoreMetric(Long userId, int atsScore, List<String> strengths, List<String> weaknesses) {
        DashboardMetrics metrics = dashboardMetricsRepository.findByUser_UserId(userId)
                .orElseGet(() -> DashboardMetrics.builder().build());
        metrics.setLatestAtsScore(atsScore);
        metrics.setStrongSkills(String.join("|||", strengths));
        metrics.setWeakSkills(String.join("|||", weaknesses));
        dashboardMetricsRepository.save(metrics);
    }

    private ResumeDTO mapToDTO(Resume resume) {
        ResumeAnalysisDTO analysisDTO = null;
        ResumeAnalysis analysis = resume.getResumeAnalysis();
        if (analysis == null) {
            analysis = resumeAnalysisRepository.findByResume_ResumeId(resume.getResumeId()).orElse(null);
        }
        if (analysis != null) {
            analysisDTO = ResumeAnalysisDTO.builder()
                    .analysisId(analysis.getAnalysisId())
                    .resumeId(resume.getResumeId())
                    .atsScore(analysis.getAtsScore())
                    .strengths(splitOrEmpty(analysis.getStrengths()))
                    .weaknesses(splitOrEmpty(analysis.getWeaknesses()))
                    .missingSkills(splitOrEmpty(analysis.getMissingSkills()))
                    .suggestions(splitOrEmpty(analysis.getSuggestions()))
                    .matchPercentage(analysis.getMatchPercentage())
                    .analyzedAt(analysis.getAnalyzedAt())
                    .build();
        }

        return ResumeDTO.builder()
                .resumeId(resume.getResumeId())
                .fileName(resume.getFileName())
                .resumeText(resume.getResumeText())
                .fileSize(resume.getFileSize())
                .uploadDate(resume.getUploadDate())
                .analysis(analysisDTO)
                .build();
    }

    private List<String> splitOrEmpty(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("\\|\\|\\|"));
    }
}
