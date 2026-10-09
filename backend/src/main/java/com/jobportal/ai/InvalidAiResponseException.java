package com.jobportal.ai;

/**
 * Raised when Ollama responds successfully but the model output is not the
 * structured JSON we asked for. Mapped to HTTP 502 (bad upstream response).
 */
public class InvalidAiResponseException extends RuntimeException {

    public InvalidAiResponseException(String message) {
        super(message);
    }

    public InvalidAiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
