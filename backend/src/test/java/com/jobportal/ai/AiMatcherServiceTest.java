package com.jobportal.ai;

import com.jobportal.config.AiProperties;
import com.jobportal.dto.AiDtos;
import com.jobportal.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiMatcherServiceTest {

    private OllamaClient ollamaClient;
    private AiMatcherService service;

    @BeforeEach
    void setUp() {
        ollamaClient = mock(OllamaClient.class);
        service = new AiMatcherService(ollamaClient, new ObjectMapper(), new AiProperties());
    }

    private AiDtos.MatchRequest request(List<String> skills, String description) {
        AiDtos.MatchRequest request = new AiDtos.MatchRequest();
        request.setSkills(skills);
        request.setJobDescription(description);
        return request;
    }

    @Test
    void mapsValidModelOutputIntoStructuredResponse() {
        when(ollamaClient.generate(anyString())).thenReturn("""
                {"matchingSkills":["Java","MySQL"],"missingSkills":["Hibernate"],
                 "improvementSuggestions":["Learn Hibernate ORM"],
                 "summary":"Strong overlap on Java and MySQL.","matchScore":72}
                """);

        AiDtos.MatchResponse response = service.match(request(
                List.of("Java", "MySQL"), "Java developer with Hibernate and MySQL."));

        assertThat(response.getMatchingSkills()).containsExactly("Java", "MySQL");
        assertThat(response.getMissingSkills()).containsExactly("Hibernate");
        assertThat(response.getImprovementSuggestions()).containsExactly("Learn Hibernate ORM");
        assertThat(response.getSummary()).isEqualTo("Strong overlap on Java and MySQL.");
        assertThat(response.getMatchScore()).isEqualTo(72);
        assertThat(response.getMatchScoreNote()).contains("approximate").contains("not an objective probability");
    }

    @Test
    void clampsOutOfRangeScores() {
        when(ollamaClient.generate(anyString())).thenReturn("{\"matchScore\":150}");

        assertThat(service.match(request(List.of("Java"), "Java")).getMatchScore()).isEqualTo(100);
    }

    @Test
    void parsesOutputWrappedInFencesAndThinking() {
        when(ollamaClient.generate(anyString())).thenReturn("""
                 thinkinglet me reason</think>
                ```json
                {"matchingSkills":["Java"],"missingSkills":[],"improvementSuggestions":[],
                 "summary":"ok","matchScore":40}
                ```
                """);

        AiDtos.MatchResponse response = service.match(request(List.of("Java"), "Java role"));

        assertThat(response.getMatchingSkills()).containsExactly("Java");
        assertThat(response.getMatchScore()).isEqualTo(40);
    }

    @Test
    void treatsMissingFieldsAsEmptyWithoutFailing() {
        when(ollamaClient.generate(anyString())).thenReturn("{\"summary\":\"partial\",\"matchScore\":10}");

        AiDtos.MatchResponse response = service.match(request(List.of("Java"), "Java role"));

        assertThat(response.getMatchingSkills()).isEmpty();
        assertThat(response.getMissingSkills()).isEmpty();
        assertThat(response.getImprovementSuggestions()).isEmpty();
        assertThat(response.getSummary()).isEqualTo("partial");
    }

    @Test
    void dropsBlankAndDuplicateOutputEntries() {
        when(ollamaClient.generate(anyString())).thenReturn("""
                {"matchingSkills":["Java","java","  ","MySQL"],"missingSkills":["Java"]}
                """);

        AiDtos.MatchResponse response = service.match(request(List.of("Java", "MySQL"), "desc"));

        assertThat(response.getMatchingSkills()).containsExactly("Java", "MySQL");
        assertThat(response.getMissingSkills()).containsExactly("Java");
    }

    @Test
    void throwsInvalidResponseForUnparseableOutput() {
        when(ollamaClient.generate(anyString())).thenReturn("I'm sorry, I cannot help with that.");

        assertThatThrownBy(() -> service.match(request(List.of("Java"), "Java role")))
                .isInstanceOf(InvalidAiResponseException.class);
    }

    @Test
    void throwsInvalidResponseForEmptyOutput() {
        when(ollamaClient.generate(anyString())).thenReturn("   ");

        assertThatThrownBy(() -> service.match(request(List.of("Java"), "Java role")))
                .isInstanceOf(InvalidAiResponseException.class);
    }

    @Test
    void propagatesOllamaUnavailable() {
        when(ollamaClient.generate(anyString()))
                .thenThrow(new OllamaUnavailableException("down"));

        assertThatThrownBy(() -> service.match(request(List.of("Java"), "Java role")))
                .isInstanceOf(OllamaUnavailableException.class);
    }

    @Test
    void rejectsEmptySkillList() {
        assertThatThrownBy(() -> service.match(request(List.of(), "Java role")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsBlankSkill() {
        assertThatThrownBy(() -> service.match(request(List.of("Java", "  "), "Java role")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsBlankJobDescription() {
        assertThatThrownBy(() -> service.match(request(List.of("Java"), "   ")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsOversizedJobDescription() {
        String tooLong = "x".repeat(4001);

        assertThatThrownBy(() -> service.match(request(List.of("Java"), tooLong)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsTooManySkills() {
        List<String> many = java.util.stream.IntStream.range(0, 51)
                .mapToObj(i -> "skill" + i)
                .toList();

        assertThatThrownBy(() -> service.match(request(many, "Java role")))
                .isInstanceOf(BadRequestException.class);
    }
}
