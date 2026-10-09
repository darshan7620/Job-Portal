package com.jobportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration for the local Ollama-backed AI matcher. All values are
 * overridable via environment variables; no model-specific behaviour is
 * hardcoded in the business logic.
 */
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** Base URL of the local Ollama server. Env: OLLAMA_BASE_URL. */
    private String baseUrl = "http://localhost:11434";

    /** Model name; must match a model pulled locally. Env: OLLAMA_MODEL. */
    private String model = "qwen3:4b";

    /** TCP connect timeout. Env: OLLAMA_CONNECT_TIMEOUT. */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** Response read timeout. Env: OLLAMA_READ_TIMEOUT. */
    private Duration readTimeout = Duration.ofSeconds(60);

    /** Upper bound on submitted job-description length, in characters. */
    private int maxJobDescriptionLength = 4000;

    /** Upper bound on the number of submitted skills. */
    private int maxSkills = 50;

    /** Upper bound on a single skill's length, in characters. */
    private int maxSkillLength = 100;

    /**
     * Whether to ask Ollama models that support reasoning to emit their thinking.
     * Disabled by default so reasoning models return the JSON answer directly.
     * Set to null to omit the field entirely for non-thinking models.
     */
    private Boolean think = Boolean.FALSE;

    /** Upper bound on generated tokens, to keep responses bounded. */
    private int maxOutputTokens = 900;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxJobDescriptionLength() {
        return maxJobDescriptionLength;
    }

    public void setMaxJobDescriptionLength(int maxJobDescriptionLength) {
        this.maxJobDescriptionLength = maxJobDescriptionLength;
    }

    public int getMaxSkills() {
        return maxSkills;
    }

    public void setMaxSkills(int maxSkills) {
        this.maxSkills = maxSkills;
    }

    public int getMaxSkillLength() {
        return maxSkillLength;
    }

    public void setMaxSkillLength(int maxSkillLength) {
        this.maxSkillLength = maxSkillLength;
    }

    public Boolean getThink() {
        return think;
    }

    public void setThink(Boolean think) {
        this.think = think;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }
}
