package com.interviewplatform.service;

import com.interviewplatform.dto.InterviewEvaluateRequest;
import com.interviewplatform.dto.InterviewEvaluationResponse;
import com.interviewplatform.dto.InterviewHistoryDTO;
import com.interviewplatform.dto.QuestionDTO;
import com.interviewplatform.dto.QuestionGenerateRequest;

import java.util.List;

public interface InterviewService {

    List<QuestionDTO> generateQuestions(QuestionGenerateRequest request);

    InterviewEvaluationResponse evaluateAnswer(InterviewEvaluateRequest request);

    List<InterviewHistoryDTO> getInterviewHistory();

    List<QuestionDTO> getQuestionHistory();
}
