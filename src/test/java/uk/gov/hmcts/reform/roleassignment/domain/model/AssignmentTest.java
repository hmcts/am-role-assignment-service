package uk.gov.hmcts.reform.roleassignment.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AssignmentTest {

    public static final String YES_HELLO = "Yes\nHello";
    public static final String HELLO = "Hello";
    public static final String YES = "Yes";
    private Assignment assignment;

    @Test
    void log() {
        assignment = new RoleAssignment();
        assignment.setLog(YES);
        assignment.log(HELLO);
        assertEquals(YES_HELLO, assignment.getLog());

        assignment.setLog(null);
        assignment.log(HELLO);
        assertEquals(HELLO, assignment.getLog());
    }

}
