package com.pvg.governance;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end API tests against a real PostgreSQL (Testcontainers), exercising the
 * seeded case study, the append-only audit trail, the ADR lifecycle, and the
 * PostgreSQL full-text search.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GovernancePlatformIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    int port;

    @Autowired
    org.springframework.boot.test.web.client.TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @Order(1)
    void seededLandscapeIsExposed() {
        ResponseEntity<List<Map<String, Object>>> response = rest.exchange(url("/api/applications"),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(7);
        assertThat(response.getBody())
                .anySatisfy(app -> assertThat(app.get("name")).isEqualTo("Central Reservation System (CRS)"));
    }

    @Test
    @Order(2)
    void seededRisksCoverAllFiveCategories() {
        ResponseEntity<List<Map<String, Object>>> response = rest.exchange(url("/api/risks"),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        assertThat(response.getBody()).hasSize(8);
        assertThat(response.getBody().stream().map(risk -> risk.get("category")))
                .contains("TECHNICAL_DEBT", "END_OF_LIFE", "INTEGRATION_BRITTLENESS",
                        "SECURITY_COMPLIANCE", "AI_READINESS");
    }

    @Test
    @Order(3)
    void staleRiskTrackingFlagsOverdueEntry() {
        ResponseEntity<List<Map<String, Object>>> response = rest.exchange(url("/api/risks/stale"),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        // Seed contains one entry assessed 200 days ago with a 90-day cadence.
        assertThat(response.getBody())
                .anySatisfy(risk -> assertThat((String) risk.get("title")).contains("lakehouse without masking"));
        response.getBody().forEach(risk -> assertThat(risk.get("stale")).isEqualTo(true));
    }

    @Test
    @Order(4)
    void topPriorityRanksByResidualScore() {
        ResponseEntity<List<Map<String, Object>>> response = rest.exchange(url("/api/risks/top-priority?limit=3"),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        List<Map<String, Object>> body = response.getBody();
        assertThat(body).hasSize(3);
        assertThat((int) body.get(0).get("residualScore"))
                .isGreaterThanOrEqualTo((int) body.get(1).get("residualScore"));
        assertThat((int) body.get(1).get("residualScore"))
                .isGreaterThanOrEqualTo((int) body.get(2).get("residualScore"));
    }

    @Test
    @Order(5)
    void updatingARiskWritesFieldLevelAuditEventsInSameTransaction() {
        List<Map<String, Object>> risks = rest.exchange(url("/api/risks"), HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {
                }).getBody();
        Map<String, Object> risk = risks.stream()
                .filter(entry -> ((String) entry.get("title")).contains("OTA rate-limit"))
                .findFirst().orElseThrow();
        String riskId = (String) risk.get("id");

        Map<String, Object> update = new java.util.HashMap<>(risk);
        update.put("status", "IN_PROGRESS");
        update.put("treatmentDecision", "INVEST");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Actor", "integration-test");
        ResponseEntity<Map<String, Object>> updated = rest.exchange(url("/api/risks/" + riskId),
                HttpMethod.PUT, new HttpEntity<>(update, headers),
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<Map<String, Object>> events = rest.exchange(
                url("/api/audit-events?entityType=RISK_ENTRY&entityId=" + riskId),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {
                }).getBody();
        assertThat(events)
                .anySatisfy(event -> {
                    assertThat(event.get("action")).isEqualTo("STATUS_CHANGED");
                    assertThat(event.get("oldValue")).isEqualTo("OPEN");
                    assertThat(event.get("newValue")).isEqualTo("IN_PROGRESS");
                    assertThat(event.get("actor")).isEqualTo("integration-test");
                })
                .anySatisfy(event -> {
                    assertThat(event.get("action")).isEqualTo("FIELD_CHANGED");
                    assertThat(event.get("fieldName")).isEqualTo("treatmentDecision");
                    assertThat(event.get("oldValue")).isEqualTo("TOLERATE");
                    assertThat(event.get("newValue")).isEqualTo("INVEST");
                })
                .anySatisfy(event -> assertThat(event.get("action")).isEqualTo("CREATED"));
    }

    @Test
    @Order(6)
    void adrLifecycleEnforcesAllowedTransitions() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> create = Map.of(
                "title", "Adopt contract testing for partner integrations",
                "context", "Partner integrations regress silently between releases.",
                "decision", "Adopt consumer-driven contract testing with a central broker.",
                "author", "Integration Test",
                "aiRelated", false);
        Map<String, Object> created = rest.exchange(url("/api/adrs"), HttpMethod.POST,
                new HttpEntity<>(create, headers),
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                }).getBody();
        assertThat(created.get("status")).isEqualTo("PROPOSED");
        assertThat((int) created.get("adrNumber")).isGreaterThan(5);
        String adrId = (String) created.get("id");

        // Invalid: PROPOSED → SUPERSEDED
        ResponseEntity<Map<String, Object>> invalid = rest.exchange(url("/api/adrs/" + adrId + "/status"),
                HttpMethod.POST, new HttpEntity<>(Map.of("status", "SUPERSEDED"), headers),
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Valid: PROPOSED → ACCEPTED
        ResponseEntity<Map<String, Object>> accepted = rest.exchange(url("/api/adrs/" + adrId + "/status"),
                HttpMethod.POST, new HttpEntity<>(Map.of("status", "ACCEPTED"), headers),
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        assertThat(accepted.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(accepted.getBody().get("status")).isEqualTo("ACCEPTED");

        List<Map<String, Object>> events = rest.exchange(
                url("/api/audit-events?entityType=ADR&entityId=" + adrId),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {
                }).getBody();
        assertThat(events)
                .anySatisfy(event -> {
                    assertThat(event.get("action")).isEqualTo("STATUS_CHANGED");
                    assertThat(event.get("newValue")).isEqualTo("ACCEPTED");
                });
    }

    @Test
    @Order(7)
    void adrFullTextSearchFindsSeededDecision() {
        ResponseEntity<List<Map<String, Object>>> response = rest.exchange(
                url("/api/adrs/search?q=prompt injection guardrails"),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        assertThat(response.getBody())
                .anySatisfy(adr -> assertThat((String) adr.get("title")).contains("AI Guest Concierge"));
    }

    @Test
    @Order(8)
    void seededSupersedesChainIsLinkedBothWays() {
        List<Map<String, Object>> adrs = rest.exchange(url("/api/adrs"), HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {
                }).getBody();
        Map<String, Object> superseded = adrs.stream()
                .filter(adr -> "SUPERSEDED".equals(adr.get("status")))
                .findFirst().orElseThrow();
        Map<String, Object> supersededBy = (Map<String, Object>) superseded.get("supersededBy");
        assertThat(supersededBy).isNotNull();
        assertThat((String) supersededBy.get("title")).contains("OpenAPI-first");
    }

    @Test
    @Order(9)
    void aiRegisterViewFiltersAiRelatedAdrs() {
        ResponseEntity<List<Map<String, Object>>> response = rest.exchange(url("/api/adrs?aiOnly=true"),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        assertThat(response.getBody()).isNotEmpty();
        response.getBody().forEach(adr -> assertThat(adr.get("aiRelated")).isEqualTo(true));
    }

    @Test
    @Order(10)
    void dashboardSummaryAggregates() {
        ResponseEntity<Map<String, Object>> response = rest.exchange(url("/api/dashboard/summary"),
                HttpMethod.GET, null,
                new org.springframework.core.ParameterizedTypeReference<>() {
                });
        Map<String, Object> body = response.getBody();
        assertThat((int) body.get("applicationCount")).isEqualTo(7);
        assertThat((int) body.get("openRiskCount")).isGreaterThanOrEqualTo(8);
        assertThat((int) body.get("staleRiskCount")).isGreaterThanOrEqualTo(1);
        assertThat((List<?>) body.get("topRisks")).isNotEmpty();
    }
}
