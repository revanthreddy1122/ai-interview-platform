package com.interviewplatform.service.impl;

import com.interviewplatform.dto.DashboardResponse;
import com.interviewplatform.dto.InterviewHistoryDTO;
import com.interviewplatform.dto.ResumeAnalysisDTO;
import com.interviewplatform.dto.ResumeDTO;
import com.interviewplatform.entity.DashboardMetrics;
import com.interviewplatform.entity.InterviewHistory;
import com.interviewplatform.entity.Resume;
import com.interviewplatform.entity.ResumeAnalysis;
import com.interviewplatform.repository.DashboardMetricsRepository;
import com.interviewplatform.repository.InterviewHistoryRepository;
import com.interviewplatform.repository.ResumeAnalysisRepository;
import com.interviewplatform.repository.ResumeRepository;
import com.interviewplatform.service.DashboardService;
import com.interviewplatform.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardMetricsRepository dashboardMetricsRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final SecurityUtil securityUtil;

    @Override
    public DashboardResponse getDashboard() {
        Long userId = securityUtil.getCurrentUserId();

        DashboardMetrics metrics = dashboardMetricsRepository.findByUser_UserId(userId)
                .orElseGet(() -> DashboardMetrics.builder()
                        .resumeCount(0)
                        .totalInterviewsAttempted(0)
                        .averageInterviewScore(0.0)
                        .build());

        List<InterviewHistoryDTO> recentInterviews = interviewHistoryRepository
                .findTop10ByUser_UserIdOrderByAttemptedAtDesc(userId)
                .stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());

        List<ResumeDTO> recentResumes = resumeRepository
                .findByUser_UserIdOrderByUploadDateDesc(userId)
                .stream()
                .limit(5)
                .map(this::mapResumeToDTO)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .latestAtsScore(metrics.getLatestAtsScore())
                .resumeCount(metrics.getResumeCount())
                .averageInterviewScore(metrics.getAverageInterviewScore())
                .totalInterviewsAttempted(metrics.getTotalInterviewsAttempted())
                .strongSkills(splitOrEmpty(metrics.getStrongSkills()))
                .weakSkills(splitOrEmpty(metrics.getWeakSkills()))
                .recentInterviews(recentInterviews)
                .recentResumes(recentResumes)
                .build();
    }

    private List<String> splitOrEmpty(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split("\\|\\|\\|"));
    }

    private InterviewHistoryDTO mapHistoryToDTO(InterviewHistory history) {
        return InterviewHistoryDTO.builder()
                .historyId(history.getHistoryId())
                .questionText(history.getQuestionText())
                .answerText(history.getAnswerText())
                .score(history.getScore())
                .correctnessScore(history.getCorrectnessScore())
                .completenessScore(history.getCompletenessScore())
                .communicationScore(history.getCommunicationScore())
                .feedback(history.getFeedback())
                .suggestions(history.getSuggestions())
                .attemptedAt(history.getAttemptedAt())
                .build();
    }

    private ResumeDTO mapResumeToDTO(Resume resume) {
        ResumeAnalysisDTO analysisDTO = null;
        ResumeAnalysis analysis = resumeAnalysisRepository.findByResume_ResumeId(resume.getResumeId()).orElse(null);
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
                .fileSize(resume.getFileSize())
                .uploadDate(resume.getUploadDate())
                .analysis(analysisDTO)
                .build();
    }
}
