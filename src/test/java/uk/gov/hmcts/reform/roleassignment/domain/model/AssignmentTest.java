package uk.gov.hmcts.reform.roleassignment.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentTest {

    private Assignment assignment;

    @Test
    void log() {
        assignment = new RoleAssignment();
        assignment.setLog("Yes");
        assignment.log("Hello");
        assertEquals(assignment.getLog(), "Yes\nHello");

        assignment.setLog(null);
        assignment.log("Hello");
        assertEquals(assignment.getLog(), "Hello");
    }

}
