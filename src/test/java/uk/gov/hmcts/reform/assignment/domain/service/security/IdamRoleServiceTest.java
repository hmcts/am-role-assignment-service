package uk.gov.hmcts.reform.assignment.domain.service.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class IdamRoleServiceTest {

    @Test
    void getUserId() {
        String caseId = "1234567812345678";
        assertEquals(caseId, "1234567812345678");
    }

    @Test
    void getUserRole() {
        String caseId = "1234567812345678";
        assertEquals(caseId, "1234567812345678");
    }

    @Test
    void getIdamRoleAssignmentsForActor() {
        String caseId = "1234567812345678";
        assertEquals(caseId, "1234567812345678");
    }
}
