package com.jobportal.ai;

import com.jobportal.config.AiProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin, isolated HTTP client for a locally running Ollama server. It owns all
 * knowledge of Ollama's wire format; the rest of the app only sees a raw model
 * string (or an {@link OllamaUnavailableException}). No paid APIs are used.
 */
@Component
public class OllamaClient {

    private static final String GENERATE_PATH = "/api/generate";

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OllamaClient(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    /**
     * Sends a single non-streaming generate request and returns the model's raw
     * text response. Throws {@link OllamaUnavailableException} on any transport,
     * timeout, HTTP-status, or envelope-format failure.
     */
    public String generate(String prompt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", properties.getModel());
        payload.put("prompt", prompt);
        payload.put("stream", false);
        payload.put("format", "json");
        // Reasoning models (e.g. qwen3) otherwise spend the whole token budget on
        // their thinking channel and return an empty answer.
        if (properties.getThink() != null) {
            payload.put("think", properties.getThink());
        }
        payload.put("options", Map.of(
                "temperature", 0.2,
                "num_predict", properties.getMaxOutputTokens()));

        String body;
        try {
            body = restClient.post()
                    .uri(GENERATE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            // Ollama answered but with an error status (e.g. unknown model).
            throw new OllamaUnavailableException("Ollama returned an error status", ex);
        } catch (ResourceAccessException ex) {
            // Connection refused, DNS failure, or read/connect timeout.
            throw new OllamaUnavailableException("Could not reach Ollama", ex);
        } catch (RestClientException ex) {
            throw new OllamaUnavailableException("Ollama request failed", ex);
        }

        return extractResponseText(body);
    }

    private String extractResponseText(String body) {
        if (body == null || body.isBlank()) {
            throw new OllamaUnavailableException("Ollama returned an empty response");
        }
        JsonNode envelope;
        try {
            envelope = objectMapper.readTree(body);
        } catch (Exception ex) {
            throw new OllamaUnavailableException("Ollama returned an unreadable response", ex);
        }
        JsonNode response = envelope == null ? null : envelope.get("response");
        if (response == null || !response.isString()) {
            throw new OllamaUnavailableException("Ollama response did not contain model output");
        }
        return response.asText();
    }
}
