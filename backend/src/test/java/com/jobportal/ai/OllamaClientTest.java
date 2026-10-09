package com.jobportal.ai;

import com.jobportal.config.AiProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OllamaClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private OllamaClient clientFor(int port, Duration readTimeout) {
        AiProperties properties = new AiProperties();
        properties.setBaseUrl("http://localhost:" + port);
        properties.setModel("test-model");
        properties.setConnectTimeout(Duration.ofSeconds(1));
        properties.setReadTimeout(readTimeout);
        return new OllamaClient(properties, new ObjectMapper());
    }

    private int startServer(HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/generate", handler);
        server.start();
        return server.getAddress().getPort();
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    @Test
    void returnsModelTextOnValidEnvelope() throws Exception {
        int port = startServer(exchange -> respond(exchange, 200,
                "{\"model\":\"test-model\",\"response\":\"{\\\"matchScore\\\":50}\",\"done\":true}"));

        String result = clientFor(port, Duration.ofSeconds(5)).generate("hello");

        assertThat(result).isEqualTo("{\"matchScore\":50}");
    }

    @Test
    void sendsJsonFormatAndThinkFlagInRequest() throws Exception {
        StringBuilder captured = new StringBuilder();
        int port = startServer(exchange -> {
            captured.append(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, "{\"response\":\"{}\"}");
        });

        clientFor(port, Duration.ofSeconds(5)).generate("hello");

        assertThat(captured.toString())
                .contains("\"format\":\"json\"")
                .contains("\"think\":false")
                .contains("\"model\":\"test-model\"");
    }

    @Test
    void throwsWhenHttpStatusIsError() throws Exception {
        int port = startServer(exchange -> respond(exchange, 500, "{\"error\":\"model not found\"}"));

        assertThatThrownBy(() -> clientFor(port, Duration.ofSeconds(5)).generate("hello"))
                .isInstanceOf(OllamaUnavailableException.class);
    }

    @Test
    void throwsWhenEnvelopeIsNotJson() throws Exception {
        int port = startServer(exchange -> respond(exchange, 200, "not-json-at-all"));

        assertThatThrownBy(() -> clientFor(port, Duration.ofSeconds(5)).generate("hello"))
                .isInstanceOf(OllamaUnavailableException.class);
    }

    @Test
    void throwsWhenResponseFieldIsMissing() throws Exception {
        int port = startServer(exchange -> respond(exchange, 200, "{\"done\":true}"));

        assertThatThrownBy(() -> clientFor(port, Duration.ofSeconds(5)).generate("hello"))
                .isInstanceOf(OllamaUnavailableException.class);
    }

    @Test
    void throwsWhenConnectionRefused() throws Exception {
        int port = startServer(exchange -> respond(exchange, 200, "{}"));
        server.stop(0);
        server = null;

        assertThatThrownBy(() -> clientFor(port, Duration.ofSeconds(1)).generate("hello"))
                .isInstanceOf(OllamaUnavailableException.class);
    }

    @Test
    void throwsWhenReadTimesOut() throws Exception {
        int port = startServer(exchange -> {
            try {
                Thread.sleep(700);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            respond(exchange, 200, "{\"response\":\"late\"}");
        });

        assertThatThrownBy(() -> clientFor(port, Duration.ofMillis(200)).generate("hello"))
                .isInstanceOf(OllamaUnavailableException.class);
    }
}
