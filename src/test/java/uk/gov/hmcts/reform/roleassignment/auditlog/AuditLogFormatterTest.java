package uk.gov.hmcts.reform.roleassignment.auditlog;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogFormatterTest {

    private AuditLogFormatter logFormatter = new AuditLogFormatter();

    @Test
    void shouldHaveCorrectLabels() throws Exception {
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setDateTime("2020-12-05 10:30:45");
        auditEntry.setOperationType("CREAT_CASE");
        auditEntry.setInvokingService("test_invokingService");
        auditEntry.setHttpMethod("GET");
        auditEntry.setPath("test_path");
        auditEntry.setHttpStatus(200);
        auditEntry.setRequestPayloadHash("239f59ed55e737c77147cf55ad0c1b030b6d7ee748a7426952f9b852d5a935e5");
        auditEntry.setAssignmentSize(1);
        auditEntry.setResponseTime(500L);
        String result = logFormatter.format(auditEntry);
        JsonNode json = JacksonUtils.MAPPER.readTree(result);
        assertEquals("LA-AM-RAS", json.get("tag").asText());
        assertEquals("2020-12-05 10:30:45", json.get("dateTime").asText());
        assertEquals("CREAT_CASE", json.get("operationType").asText());
        assertEquals(1, json.get("assignmentSize").asInt());
        assertEquals("test_invokingService", json.get("invokingService").asText());
        assertEquals("test_path", json.get("endpointCalled").asText());
        assertEquals(200, json.get("operationalOutcome").asInt());
        assertEquals("239f59ed55e737c77147cf55ad0c1b030b6d7ee748a7426952f9b852d5a935e5",
            json.get("requestPayloadHash").asText());
        assertEquals(500L, json.get("responseTime").asLong());
    }

    @Test
    void shouldNotLogFieldIfEmpty() throws Exception {
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setOperationType("CREAT_CASE");

        String result = logFormatter.format(auditEntry);
        JsonNode json = JacksonUtils.MAPPER.readTree(result);

        assertThat(json.get("operationType").asText()).isEqualTo("CREAT_CASE");
        assertThat(json.has("caseId")).isFalse();
        assertThat(json.has("requestPayloadHash")).isFalse();

    }

    @Test
    void shouldEscapePotentiallyMaliciousPayload() throws Exception {
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setRequestPayloadHash("881d91b5c9c7bc6edbce9f98e71fc18611d0c344e3bd1d2224c10a13580e1073");

        String result = logFormatter.format(auditEntry);
        JsonNode json = JacksonUtils.MAPPER.readTree(result);

        assertThat(json.get("requestPayloadHash").asText())
            .isEqualTo("881d91b5c9c7bc6edbce9f98e71fc18611d0c344e3bd1d2224c10a13580e1073");
    }


    @Test
    void shouldHandleNullTargetCaseRoles() throws Exception {
        AuditEntry auditEntry = new AuditEntry();

        String result = logFormatter.format(auditEntry);
        JsonNode json = JacksonUtils.MAPPER.readTree(result);

        assertThat(json.get("tag").asText()).isEqualTo("LA-AM-RAS");
        assertThat(json.has("targetCaseRoles")).isFalse();
    }
}
