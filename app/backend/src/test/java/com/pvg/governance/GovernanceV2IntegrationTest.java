package com.pvg.governance;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for the v2 modules (proposal §3.3–§3.5 and §6 items 4–6):
 * Advice Forum workflow with arbitration, Standards library versioning, C4
 * diagram versions, fitness-function check status pushes, and idempotent
 * read-only ADR repository indexing.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GovernanceV2IntegrationTest {

    static final String CRS_APP_ID = "11111111-1111-1111-1111-111111111101";
    static final String PLANNED_SESSION_ID = "55555555-5555-5555-5555-555555555502";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Actor", "v2-integration-test");
        return headers;
    }

    private List<Map<String, Object>> getList(String path) {
        return rest.exchange(url(path), HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }).getBody();
    }

    private ResponseEntity<Map<String, Object>> post(String path, Map<String, Object> body) {
        return rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()),
                new ParameterizedTypeReference<>() {
                });
    }

    private ResponseEntity<Map<String, Object>> put(String path, Map<String, Object> body) {
        return rest.exchange(url(path), HttpMethod.PUT, new HttpEntity<>(body, jsonHeaders()),
                new ParameterizedTypeReference<>() {
                });
    }

    @Test
    @Order(1)
    void seededForumWorkflowIsExposed() {
        assertThat(getList("/api/forum/sessions")).hasSize(2);
        List<Map<String, Object>> proposals = getList("/api/forum/proposals");
        assertThat(proposals).hasSize(4);
        assertThat(proposals.stream().map(p -> p.get("status")))
                .contains("SUBMITTED", "SCHEDULED", "DECIDED");
        // Arbitration is visible — and visibly rare (one record in the whole seed).
        List<Map<String, Object>> arbitrations = getList("/api/forum/arbitrations");
        assertThat(arbitrations).hasSize(1);
        assertThat((String) arbitrations.get(0).get("proposalTitle")).contains("payment orchestration");
    }

    @Test
    @Order(2)
    void proposalWorkflowSchedulesCapturesOutcomeAndAudits() {
        Map<String, Object> created = post("/api/forum/proposals", Map.of(
                "title", "Introduce a group-wide API gateway",
                "summary", "Seeking advice on consolidating ingress across brands.",
                "scope", "DOMAIN",
                "submittedBy", "Platform Team Lead",
                "applicationId", CRS_APP_ID)).getBody();
        assertThat(created.get("status")).isEqualTo("SUBMITTED");
        String id = (String) created.get("id");

        // Outcome before scheduling is rejected: the forum is the venue.
        ResponseEntity<Map<String, Object>> premature = post("/api/forum/proposals/" + id + "/outcome", Map.of(
                "adviceGiven", "x", "advisedBy", "y", "finalDecision", "z"));
        assertThat(premature.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        Map<String, Object> scheduled = post("/api/forum/proposals/" + id + "/schedule",
                Map.of("sessionId", PLANNED_SESSION_ID)).getBody();
        assertThat(scheduled.get("status")).isEqualTo("SCHEDULED");

        Map<String, Object> decided = post("/api/forum/proposals/" + id + "/outcome", Map.of(
                "adviceGiven", "Start with the two brands already on the shared platform.",
                "advisedBy", "Group IT/Software Architect; Brand CTOs",
                "finalDecision", "Proceed with a two-brand pilot before any mandate.")).getBody();
        assertThat(decided.get("status")).isEqualTo("DECIDED");
        assertThat(decided.get("decidedAt")).isNotNull();

        List<Map<String, Object>> events = getList("/api/audit-events?entityType=AAF_PROPOSAL&entityId=" + id);
        assertThat(events)
                .anySatisfy(event -> assertThat(event.get("action")).isEqualTo("CREATED"))
                .anySatisfy(event -> {
                    assertThat(event.get("action")).isEqualTo("STATUS_CHANGED");
                    assertThat(event.get("newValue")).isEqualTo("SCHEDULED");
                })
                .anySatisfy(event -> {
                    assertThat(event.get("action")).isEqualTo("STATUS_CHANGED");
                    assertThat(event.get("newValue")).isEqualTo("DECIDED");
                    assertThat(event.get("actor")).isEqualTo("v2-integration-test");
                });
    }

    @Test
    @Order(3)
    void arbitrationIsGroupScopeOnlyEscalation() {
        Map<String, Object> teamProposal = post("/api/forum/proposals", Map.of(
                "title", "Team-scope build tool change",
                "summary", "Local decision.",
                "scope", "TEAM",
                "submittedBy", "Team Lead")).getBody();
        ResponseEntity<Map<String, Object>> rejected = post(
                "/api/forum/proposals/" + teamProposal.get("id") + "/arbitration",
                Map.of("requestedBy", "Someone", "rationale", "r", "outcome", "o", "decidedBy", "d"));
        assertThat(rejected.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        Map<String, Object> groupProposal = post("/api/forum/proposals", Map.of(
                "title", "Group-wide observability platform mandate",
                "summary", "Unresolved disagreement between brands.",
                "scope", "GROUP",
                "submittedBy", "Head of SRE")).getBody();
        String groupId = (String) groupProposal.get("id");
        ResponseEntity<Map<String, Object>> arbitration = post(
                "/api/forum/proposals/" + groupId + "/arbitration",
                Map.of("requestedBy", "Head of SRE",
                        "rationale", "Brands cannot agree; group cost exposure grows monthly.",
                        "outcome", "Mandate upheld with phased onboarding.",
                        "decidedBy", "Group CTO"));
        assertThat(arbitration.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> decided = rest.exchange(url("/api/forum/proposals/" + groupId),
                HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {
                }).getBody();
        assertThat(decided.get("status")).isEqualTo("DECIDED");
        assertThat(decided.get("arbitrated")).isEqualTo(true);
        assertThat(getList("/api/forum/arbitrations")).hasSize(2);

        // A second arbitration of the same proposal is rejected.
        ResponseEntity<Map<String, Object>> duplicate = post(
                "/api/forum/proposals/" + groupId + "/arbitration",
                Map.of("requestedBy", "x", "rationale", "r", "outcome", "o", "decidedBy", "d"));
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(4)
    void seededStandardsExposeAdrAndAdoptionLinks() {
        List<Map<String, Object>> standards = getList("/api/standards");
        assertThat(standards).hasSize(4);
        Map<String, Object> apiConventions = standards.stream()
                .filter(standard -> "REST API conventions".equals(standard.get("title")))
                .findFirst().orElseThrow();
        assertThat((List<Map<String, Object>>) apiConventions.get("linkedAdrs"))
                .anySatisfy(adr -> assertThat((String) adr.get("title")).contains("OpenAPI-first"));
        assertThat((List<?>) apiConventions.get("appliedApplications")).hasSize(3);
    }

    @Test
    @Order(5)
    void standardContentChangeBumpsVersionAndRetiredIsImmutable() {
        Map<String, Object> created = post("/api/standards", Map.of(
                "title", "Secrets management standard",
                "category", "SECURITY",
                "content", "v1: secrets live in the group vault.",
                "owner", "Group CISO")).getBody();
        String id = (String) created.get("id");
        assertThat(created.get("status")).isEqualTo("DRAFT");
        assertThat((int) created.get("version")).isEqualTo(1);

        Map<String, Object> updated = put("/api/standards/" + id, Map.of(
                "title", "Secrets management standard",
                "category", "SECURITY",
                "content", "v2: secrets live in the group vault; rotation is automated.",
                "owner", "Group CISO")).getBody();
        assertThat((int) updated.get("version")).isEqualTo(2);

        List<Map<String, Object>> events = getList("/api/audit-events?entityType=STANDARD&entityId=" + id);
        assertThat(events).anySatisfy(event -> {
            assertThat(event.get("fieldName")).isEqualTo("content");
            assertThat((String) event.get("oldValue")).contains("v1");
            assertThat((String) event.get("newValue")).contains("v2");
        });

        assertThat(post("/api/standards/" + id + "/status", Map.of("status", "ACTIVE"))
                .getBody().get("status")).isEqualTo("ACTIVE");
        // DRAFT → RETIRED style jumps are invalid; ACTIVE → RETIRED is allowed.
        assertThat(post("/api/standards/" + id + "/status", Map.of("status", "ACTIVE"))
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(post("/api/standards/" + id + "/status", Map.of("status", "RETIRED"))
                .getBody().get("status")).isEqualTo("RETIRED");
        ResponseEntity<Map<String, Object>> editRetired = put("/api/standards/" + id, Map.of(
                "title", "Secrets management standard",
                "category", "SECURITY",
                "content", "v3 attempt",
                "owner", "Group CISO"));
        assertThat(editRetired.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(6)
    void c4DiagramVersionsAreAppendOnly() {
        List<Map<String, Object>> versions = getList("/api/applications/" + CRS_APP_ID + "/c4");
        assertThat(versions).hasSize(2);
        assertThat((int) versions.get(0).get("version")).isEqualTo(2);

        Map<String, Object> added = post("/api/applications/" + CRS_APP_ID + "/c4", Map.of(
                "label", "System context — CRS (post parallel run)",
                "source", "C4Context\n    title CRS v3\n    System(crs, \"CRS\", \"Bookings\")")).getBody();
        assertThat((int) added.get("version")).isEqualTo(3);
        assertThat(added.get("author")).isEqualTo("v2-integration-test");
        assertThat(getList("/api/applications/" + CRS_APP_ID + "/c4")).hasSize(3);
    }

    @Test
    @Order(7)
    void checkStatusPushUpdatesStatusAndKeepsHistoryInAuditTrail() {
        List<Map<String, Object>> checks = getList("/api/applications/" + CRS_APP_ID + "/checks");
        assertThat(checks).hasSize(2);
        Map<String, Object> failing = checks.stream()
                .filter(check -> "FAILING".equals(check.get("lastStatus")))
                .findFirst().orElseThrow();
        String checkId = (String) failing.get("id");

        Map<String, Object> updated = post("/api/checks/" + checkId + "/status",
                Map.of("status", "PASSING")).getBody();
        assertThat(updated.get("lastStatus")).isEqualTo("PASSING");
        assertThat(updated.get("lastRunAt")).isNotNull();

        List<Map<String, Object>> events = getList("/api/audit-events?entityType=CHECK_REFERENCE&entityId=" + checkId);
        assertThat(events).anySatisfy(event -> {
            assertThat(event.get("action")).isEqualTo("STATUS_CHANGED");
            assertThat(event.get("oldValue")).isEqualTo("FAILING");
            assertThat(event.get("newValue")).isEqualTo("PASSING");
        });
    }

    @Test
    @Order(8)
    void adrRepositoryImportIsIdempotentAndReadOnly() {
        String markdown = """
                # Use PostgreSQL logical replication for reporting

                ## Status
                Accepted

                ## Context
                Reporting queries degrade the operational database.

                ## Decision
                Stream changes to a reporting replica via logical replication.

                ## Consequences
                Reporting lags by seconds, not hours; replica schema must track migrations.

                ## Tags
                data,replication
                """;
        Map<String, Object> imported = post("/api/adr-imports", Map.of(
                "repoUrl", "https://github.com/pvg-demo/data-platform",
                "documents", List.of(Map.of("path", "docs/adr/0003-logical-replication.md", "markdown", markdown))))
                .getBody();
        assertThat((int) imported.get("created")).isEqualTo(1);
        List<Map<String, Object>> results = (List<Map<String, Object>>) imported.get("results");
        String adrId = (String) results.get(0).get("adrId");

        // Re-importing the identical file changes nothing and duplicates nothing.
        Map<String, Object> reimported = post("/api/adr-imports", Map.of(
                "repoUrl", "https://github.com/pvg-demo/data-platform",
                "documents", List.of(Map.of("path", "docs/adr/0003-logical-replication.md", "markdown", markdown))))
                .getBody();
        assertThat((int) reimported.get("created")).isZero();
        assertThat((int) reimported.get("updated")).isZero();

        // An edited file updates the same record in place.
        Map<String, Object> changed = post("/api/adr-imports", Map.of(
                "repoUrl", "https://github.com/pvg-demo/data-platform",
                "documents", List.of(Map.of("path", "docs/adr/0003-logical-replication.md",
                        "markdown", markdown.replace("seconds, not hours", "under a minute")))))
                .getBody();
        assertThat((int) changed.get("updated")).isEqualTo(1);
        List<Map<String, Object>> changedResults = (List<Map<String, Object>>) changed.get("results");
        assertThat(changedResults.get(0).get("adrId")).isEqualTo(adrId);

        Map<String, Object> adr = rest.exchange(url("/api/adrs/" + adrId), HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }).getBody();
        assertThat(adr.get("source")).isEqualTo("REPOSITORY");
        assertThat(adr.get("sourcePath")).isEqualTo("docs/adr/0003-logical-replication.md");
        assertThat((String) adr.get("consequences")).contains("under a minute");

        // The indexed copy is read-only in the platform: no edits, no status changes.
        Map<String, Object> editAttempt = new HashMap<>();
        editAttempt.put("title", "hijacked");
        editAttempt.put("context", "x");
        editAttempt.put("decision", "y");
        editAttempt.put("aiRelated", false);
        assertThat(put("/api/adrs/" + adrId, editAttempt).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(post("/api/adrs/" + adrId + "/status", Map.of("status", "DEPRECATED"))
                .getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @Order(9)
    void seededRepositoryAdrIsSearchableCaseLaw() {
        List<Map<String, Object>> hits = getList("/api/adrs/search?q=tokenisation vault");
        assertThat(hits).anySatisfy(adr -> {
            assertThat(adr.get("source")).isEqualTo("REPOSITORY");
            assertThat((String) adr.get("sourceRepoUrl")).contains("payment-orchestration");
        });
    }

    @Test
    @Order(10)
    void dashboardAggregatesV2Modules() {
        Map<String, Object> summary = rest.exchange(url("/api/dashboard/summary"), HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }).getBody();
        assertThat((int) summary.get("openProposalCount")).isGreaterThanOrEqualTo(1);
        assertThat((int) summary.get("arbitrationCount")).isGreaterThanOrEqualTo(1);
        assertThat((int) summary.get("activeStandardCount")).isGreaterThanOrEqualTo(3);
        assertThat((int) summary.get("failingCheckCount")).isGreaterThanOrEqualTo(0);
    }
}
