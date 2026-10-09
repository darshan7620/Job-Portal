package com.jobportal.ai;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Maps AI-integration failures to responses that keep the rest of the app
 * usable. Ordered first so these specific handlers win over the global
 * catch-all handler.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AiExceptionHandler {

    @ExceptionHandler(OllamaUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleOllamaUnavailable(OllamaUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "detail",
                "The AI matcher is temporarily unavailable. Make sure Ollama is running and the "
                        + "configured model has been pulled, then try again."));
    }

    @ExceptionHandler(InvalidAiResponseException.class)
    public ResponseEntity<Map<String, String>> handleInvalidResponse(InvalidAiResponseException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "detail", "The AI returned a response we could not understand. Please try again."));
    }
}
