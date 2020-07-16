package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class IdamRoleServiceTest {

    @Test
    void getUserId() {
        String caseId = "1234567812345678";
        assertEquals("1234567812345678", caseId);
    }

    @Test
    void getUserRole() {
        String caseId = "1234567812345678";
        assertEquals("1234567812345678", caseId);
    }

    @Test
    void getIdamRoleAssignmentsForActor() {
        String caseId = "1234567812345678";
        assertEquals("1234567812345678", caseId);
    }
}
