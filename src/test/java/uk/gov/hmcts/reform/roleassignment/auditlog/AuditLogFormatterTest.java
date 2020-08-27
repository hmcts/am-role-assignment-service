package uk.gov.hmcts.reform.roleassignment.auditlog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogFormatterTest {

    private AuditLogFormatter logFormatter = new AuditLogFormatter();

    @Test
    void shouldHaveCorrectLabels() {
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setDateTime("2020-12-05 10:30:45");
        auditEntry.setOperationType("CREAT_CASE");
        auditEntry.setInvokingService("test_invokingService");
        auditEntry.setHttpMethod("GET");
        auditEntry.setPath("test_path");
        auditEntry.setHttpStatus(200);
        String result = logFormatter.format(auditEntry);
        assertEquals("LA-AM-RAS dateTime:2020-12-05 10:30:45,"
                         + "operationType:CREAT_CASE,"
                         + "invokingService:test_invokingService,"
                         + "endpointCalled:GET test_path,"
                         + "operationalOutcome:200", result);
    }

    @Test
    void shouldNotLogPairIfEmpty() {
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setOperationType("CREAT_CASE");

        String result = logFormatter.format(auditEntry);

        assertThat(result).containsOnlyOnce("operationType:CREAT_CASE");
        assertThat(result).doesNotContainPattern("caseId:");
    }

    @Test
    void shouldHandleListWithComma() {
        AuditEntry auditEntry = new AuditEntry();

        String result = logFormatter.format(auditEntry);

        assertThat(result).containsOnlyOnce("LA-AM-RAS ,");
    }


    @Test
    void shouldHandleNullTargetCaseRoles() {
        AuditEntry auditEntry = new AuditEntry();

        String result = logFormatter.format(auditEntry);

        assertThat(result).doesNotContain("targetCaseRoles:role1,role2");
    }
}




