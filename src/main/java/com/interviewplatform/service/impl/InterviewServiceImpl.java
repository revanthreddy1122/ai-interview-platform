package com.interviewplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.interviewplatform.dto.InterviewEvaluateRequest;
import com.interviewplatform.dto.InterviewEvaluationResponse;
import com.interviewplatform.dto.InterviewHistoryDTO;
import com.interviewplatform.dto.QuestionDTO;
import com.interviewplatform.dto.QuestionGenerateRequest;
import com.interviewplatform.entity.DashboardMetrics;
import com.interviewplatform.entity.InterviewHistory;
import com.interviewplatform.entity.Question;
import com.interviewplatform.entity.QuestionCategory;
import com.interviewplatform.entity.User;
import com.interviewplatform.repository.DashboardMetricsRepository;
import com.interviewplatform.repository.InterviewHistoryRepository;
import com.interviewplatform.repository.QuestionRepository;
import com.interviewplatform.service.AIService;
import com.interviewplatform.service.InterviewService;
import com.interviewplatform.util.JsonParserUtil;
import com.interviewplatform.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final QuestionRepository questionRepository;
    private final InterviewHistoryRepository interviewHistoryRepository;
    private final DashboardMetricsRepository dashboardMetricsRepository;
    private final AIService aiService;
    private final JsonParserUtil jsonParserUtil;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public List<QuestionDTO> generateQuestions(QuestionGenerateRequest request) {
        User currentUser = securityUtil.getCurrentUser();

        QuestionCategory category = resolveCategory(request.getCategory());

        List<String> generatedQuestions = aiService.generateQuestions(
                category.name(),
                request.getDifficulty(),
                request.getResumeContext(),
                request.getCount()
        );

        List<Question> savedQuestions = generatedQuestions.stream()
                .map(text -> Question.builder()
                        .user(currentUser)
                        .questionText(text)
                        .category(category)
                        .difficulty(request.getDifficulty() == null ? "MIXED" : request.getDifficulty())
                        .build())
                .map(questionRepository::save)
                .collect(Collectors.toList());

        log.info("Generated {} questions for user {} in category {}",
                savedQuestions.size(), currentUser.getUserId(), category);

        return savedQuestions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InterviewEvaluationResponse evaluateAnswer(InterviewEvaluateRequest request) {
        User currentUser = securityUtil.getCurrentUser();

        JsonNode aiResult = aiService.evaluateAnswer(request.getQuestion(), request.getAnswer());

        int correctness = jsonParserUtil.safeGetInt(aiResult, "correctnessScore", 0);
        int completeness = jsonParserUtil.safeGetInt(aiResult, "completenessScore", 0);
        int communication = jsonParserUtil.safeGetInt(aiResult, "communicationScore", 0);
        int overallScore = jsonParserUtil.safeGetInt(aiResult, "overallScore",
                Math.round((correctness + completeness + communication) / 3.0f));
        String feedback = jsonParserUtil.safeGetText(aiResult, "feedback", "No feedback available");
        String suggestions = jsonParserUtil.safeGetText(aiResult, "suggestions", "No suggestions available");

        Question linkedQuestion = null;
        if (request.getQuestionId() != null) {
            linkedQuestion = questionRepository.findById(request.getQuestionId()).orElse(null);
        }

        InterviewHistory history = InterviewHistory.builder()
                .user(currentUser)
                .question(linkedQuestion)
                .questionText(request.getQuestion())
                .answerText(request.getAnswer())
                .score(overallScore)
                .correctnessScore(correctness)
                .completenessScore(completeness)
                .communicationScore(communication)
                .feedback(feedback)
                .suggestions(suggestions)
                .build();

        InterviewHistory savedHistory = interviewHistoryRepository.save(history);

        updateInterviewMetrics(currentUser.getUserId());

        log.info("Interview answer evaluated for user {} with score {}", currentUser.getUserId(), overallScore);

        return InterviewEvaluationResponse.builder()
                .historyId(savedHistory.getHistoryId())
                .score(overallScore)
                .correctnessScore(correctness)
                .completenessScore(completeness)
                .communicationScore(communication)
                .feedback(feedback)
                .suggestions(suggestions)
                .build();
    }

    @Override
    public List<InterviewHistoryDTO> getInterviewHistory() {
        Long userId = securityUtil.getCurrentUserId();
        return interviewHistoryRepository.findByUser_UserIdOrderByAttemptedAtDesc(userId)
                .stream()
                .map(this::mapHistoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionDTO> getQuestionHistory() {
        Long userId = securityUtil.getCurrentUserId();
        return questionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private QuestionCategory resolveCategory(String category) {
        if (category == null || category.isBlank()) {
            return QuestionCategory.GENERAL;
        }
        try {
            return QuestionCategory.valueOf(category.trim().toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException ex) {
            return QuestionCategory.GENERAL;
        }
    }

    private void updateInterviewMetrics(Long userId) {
        DashboardMetrics metrics = dashboardMetricsRepository.findByUser_UserId(userId)
                .orElseGet(() -> DashboardMetrics.builder().build());
        long totalAttempted = interviewHistoryRepository.countByUser_UserId(userId);
        Double averageScore = interviewHistoryRepository.findAverageScoreByUserId(userId);
        metrics.setTotalInterviewsAttempted((int) totalAttempted);
        metrics.setAverageInterviewScore(averageScore == null ? 0.0 : averageScore);
        dashboardMetricsRepository.save(metrics);
    }

    private QuestionDTO mapToDTO(Question question) {
        return QuestionDTO.builder()
                .questionId(question.getQuestionId())
                .questionText(question.getQuestionText())
                .category(question.getCategory().name())
                .difficulty(question.getDifficulty())
                .createdAt(question.getCreatedAt())
                .build();
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
}
