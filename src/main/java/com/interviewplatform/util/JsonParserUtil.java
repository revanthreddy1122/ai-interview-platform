package com.interviewplatform.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewplatform.exception.AIServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class JsonParserUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Pattern JSON_BLOCK_PATTERN =
            Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```", Pattern.MULTILINE);
    private static final Pattern GENERIC_BLOCK_PATTERN =
            Pattern.compile("```\\s*([\\s\\S]*?)\\s*```", Pattern.MULTILINE);

    public JsonNode parseJsonFromAiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            throw new AIServiceException("AI service returned an empty response");
        }

        String cleaned = extractJsonContent(rawResponse);

        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception ex) {
            log.error("Failed to parse AI JSON response. Raw content: {}", rawResponse);
            throw new AIServiceException("Failed to parse AI response as JSON: " + ex.getMessage(), ex);
        }
    }

    private String extractJsonContent(String rawResponse) {
        Matcher jsonMatcher = JSON_BLOCK_PATTERN.matcher(rawResponse);
        if (jsonMatcher.find()) {
            return jsonMatcher.group(1).trim();
        }

        Matcher genericMatcher = GENERIC_BLOCK_PATTERN.matcher(rawResponse);
        if (genericMatcher.find()) {
            return genericMatcher.group(1).trim();
        }

        String trimmed = rawResponse.trim();
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }

        return trimmed;
    }

    public List<String> nodeToStringList(JsonNode node) {
        List<String> result = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                result.add(item.asText());
            }
        } else if (node != null && node.isTextual()) {
            result.add(node.asText());
        }
        return result;
    }

    public int safeGetInt(JsonNode node, String fieldName, int defaultValue) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return defaultValue;
        }
        try {
            return node.get(fieldName).asInt(defaultValue);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public String safeGetText(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || !node.has(fieldName) || node.get(fieldName).isNull()) {
            return defaultValue;
        }
        return node.get(fieldName).asText(defaultValue);
    }
}
