package com.interviewplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewplatform.exception.AIServiceException;
import com.interviewplatform.service.AIService;
import com.interviewplatform.util.JsonParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OpenRouterAIServiceImpl implements AIService {

    private final WebClient webClient;
    private final JsonParserUtil jsonParserUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.api.model}")
    private String model;

    public OpenRouterAIServiceImpl(WebClient.Builder webClientBuilder, JsonParserUtil jsonParserUtil) {
        this.webClient = webClientBuilder.build();
        this.jsonParserUtil = jsonParserUtil;
    }

    @Override
    public JsonNode analyzeResume(String resumeText) {
        String prompt = buildResumeAnalysisPrompt(resumeText);
        String rawResponse = callOpenRouter(prompt);
        return jsonParserUtil.parseJsonFromAiResponse(rawResponse);
    }

    @Override
    public JsonNode matchJobDescription(String resumeText, String jobDescription) {
        String prompt = buildJobMatchPrompt(resumeText, jobDescription);
        String rawResponse = callOpenRouter(prompt);
        return jsonParserUtil.parseJsonFromAiResponse(rawResponse);
    }

    @Override
    public List<String> generateQuestions(String category, String difficulty, String resumeContext, int count) {
        String prompt = buildQuestionGenerationPrompt(category, difficulty, resumeContext, count);
        String rawResponse = callOpenRouter(prompt);
        JsonNode root = jsonParserUtil.parseJsonFromAiResponse(rawResponse);

        List<String> questions = new ArrayList<>();
        JsonNode questionsNode = root.has("questions") ? root.get("questions") : root;
        if (questionsNode.isArray()) {
            for (JsonNode q : questionsNode) {
                if (q.isTextual()) {
                    questions.add(q.asText());
                } else if (q.has("question")) {
                    questions.add(q.get("question").asText());
                }
            }
        }

        if (questions.isEmpty()) {
            throw new AIServiceException("AI service did not return any valid questions");
        }

        return questions;
    }

    @Override
    public JsonNode evaluateAnswer(String question, String answer) {
        String prompt = buildAnswerEvaluationPrompt(question, answer);
        String rawResponse = callOpenRouter(prompt);
        return jsonParserUtil.parseJsonFromAiResponse(rawResponse);
    }

    private String callOpenRouter(String prompt) {
        try {
            String requestBody = buildOpenRouterRequestBody(prompt);

            String response = webClient.post()
                    .uri(apiUrl)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "AI Interview Preparation Platform")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(3))
                            .filter(this::isRetryable))
                    .block();

            return extractTextFromOpenRouterResponse(response);
        } catch (WebClientResponseException ex) {
            log.error("OpenRouter API call failed with status {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AIServiceException(
                    "AI service request failed with status " + ex.getStatusCode() + ". Please try again later.", ex);
        } catch (AIServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error calling OpenRouter API: {}", ex.getMessage(), ex);
            throw new AIServiceException("AI service is currently unavailable: " + ex.getMessage(), ex);
        }
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException wcre) {
            int status = wcre.getStatusCode().value();
            return status == 429 || status >= 500;
        }
        return false;
    }

    /**
     * Builds an OpenAI-compatible chat completions request body.
     * OpenRouter uses the same format as OpenAI: /v1/chat/completions with
     * a "messages" array containing role/content objects.
     */
    private String buildOpenRouterRequestBody(String prompt) {
        try {
            var root = objectMapper.createObjectNode();
            root.put("model", model);

            var messages = objectMapper.createArrayNode();
            var systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "You are an expert AI assistant specializing in technical recruitment, " +
                    "resume analysis, and interview preparation. " +
                    "Always respond with valid JSON only — no markdown, no code fences, no extra text.");
            messages.add(systemMsg);

            var userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            root.set("messages", messages);
            root.put("temperature", 0.4);
            root.put("max_tokens", 2048);
            root.put("top_p", 0.9);

            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            throw new AIServiceException("Failed to build AI request payload: " + ex.getMessage(), ex);
        }
    }

    /**
     * Extracts the assistant message text from the OpenAI-compatible response:
     * choices[0].message.content
     */
    private String extractTextFromOpenRouterResponse(String rawJsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawJsonResponse);

            // Surface any API-level error returned in the body
            if (root.has("error")) {
                String errorMsg = root.path("error").path("message").asText("Unknown API error");
                throw new AIServiceException("OpenRouter API error: " + errorMsg);
            }

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                log.error("OpenRouter response had no choices. Full response: {}", rawJsonResponse);
                throw new AIServiceException("AI service returned no choices in the response");
            }

            String content = choices.get(0).path("message").path("content").asText("").trim();

            if (content.isBlank()) {
                // Check finish_reason for clues
                String finishReason = choices.get(0).path("finish_reason").asText("");
                log.error("OpenRouter returned empty content. finish_reason={}, response={}", finishReason, rawJsonResponse);
                throw new AIServiceException("AI service returned an empty response (finish_reason: " + finishReason + ")");
            }

            return content;
        } catch (AIServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to parse OpenRouter response envelope: {}", rawJsonResponse, ex);
            throw new AIServiceException("Failed to parse AI service response: " + ex.getMessage(), ex);
        }
    }

    private String buildResumeAnalysisPrompt(String resumeText) {
        return """
                Analyze the following resume text thoroughly as an expert ATS system and technical recruiter.

                Resume Text:
                \"\"\"
                %s
                \"\"\"

                Return ONLY a valid JSON object with EXACTLY this structure and nothing else
                (no markdown, no explanations outside the JSON):

                {
                  "atsScore": <integer 0-100 representing ATS compatibility score>,
                  "strengths": [<array of 3-6 short strings describing resume strengths>],
                  "weaknesses": [<array of 3-6 short strings describing resume weaknesses>],
                  "missingSkills": [<array of relevant in-demand technical or soft skills missing from the resume>],
                  "suggestions": [<array of 4-8 actionable improvement suggestions>]
                }

                Base the ATS score on keyword density, formatting clarity, quantifiable achievements, and structure.
                """.formatted(resumeText);
    }

    private String buildJobMatchPrompt(String resumeText, String jobDescription) {
        return """
                Compare this candidate resume against the job description as an expert technical recruiter.

                Resume Text:
                \"\"\"
                %s
                \"\"\"

                Job Description:
                \"\"\"
                %s
                \"\"\"

                Return ONLY a valid JSON object with EXACTLY this structure and nothing else
                (no markdown, no explanations outside the JSON):

                {
                  "matchPercentage": <integer 0-100 representing how well the resume matches the job description>,
                  "missingSkills": [<array of skills/keywords present in the job description but missing from the resume>],
                  "suggestions": [<array of 4-8 actionable suggestions to improve the match>]
                }

                Base the match percentage on overlap of required skills, experience level,
                and keyword alignment between the resume and the job description.
                """.formatted(resumeText, jobDescription);
    }

    private String buildQuestionGenerationPrompt(String category, String difficulty, String resumeContext, int count) {
        String difficultyText = (difficulty == null || difficulty.isBlank()) ? "mixed (easy, medium, hard)" : difficulty;
        String contextSection = (resumeContext == null || resumeContext.isBlank())
                ? "No resume context provided. Generate general questions for the category."
                : "Use this candidate resume context to tailor relevant questions:\n\"\"\"\n" + resumeContext + "\n\"\"\"";

        return """
                You are a senior technical interviewer preparing interview questions.

                Category: %s
                Difficulty: %s
                %s

                Generate exactly %d interview questions for this category and difficulty level.

                Return ONLY a valid JSON object with EXACTLY this structure and nothing else
                (no markdown, no explanations outside the JSON):

                {
                  "questions": [<array of exactly %d strings, each a complete, well-formed interview question>]
                }
                """.formatted(category, difficultyText, contextSection, count, count);
    }

    private String buildAnswerEvaluationPrompt(String question, String answer) {
        return """
                You are a senior technical interviewer evaluating a candidate's interview answer.

                Question:
                \"\"\"
                %s
                \"\"\"

                Candidate Answer:
                \"\"\"
                %s
                \"\"\"

                Evaluate the answer on three dimensions: correctness (technical accuracy), completeness
                (how thoroughly the question was addressed), and communication (clarity and structure).

                Return ONLY a valid JSON object with EXACTLY this structure and nothing else
                (no markdown, no explanations outside the JSON):

                {
                  "correctnessScore": <integer 0-100>,
                  "completenessScore": <integer 0-100>,
                  "communicationScore": <integer 0-100>,
                  "overallScore": <integer 0-100, weighted overall score>,
                  "feedback": "<2-4 sentence constructive feedback on the answer>",
                  "suggestions": "<1-3 sentence specific suggestion on how to improve the answer>"
                }

                If the candidate answer is empty or says I don't know, score low but give constructive feedback.
                """.formatted(question, answer);
    }
}
