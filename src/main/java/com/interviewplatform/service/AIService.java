package com.interviewplatform.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface AIService {

    JsonNode analyzeResume(String resumeText);

    JsonNode matchJobDescription(String resumeText, String jobDescription);

    List<String> generateQuestions(String category, String difficulty, String resumeContext, int count);

    JsonNode evaluateAnswer(String question, String answer);
}
