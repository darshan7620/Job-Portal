package com.jobportal.ai;

/**
 * Raised when the local Ollama server cannot be reached, times out, returns an
 * HTTP error, or replies with an envelope we cannot read. Mapped to HTTP 503 so
 * the rest of the application keeps working when the AI backend is down.
 */
public class OllamaUnavailableException extends RuntimeException {

    public OllamaUnavailableException(String message) {
        super(message);
    }

    public OllamaUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
