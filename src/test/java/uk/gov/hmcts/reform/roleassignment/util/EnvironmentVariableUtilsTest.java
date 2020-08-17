package uk.gov.hmcts.reform.roleassignment.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class EnvironmentVariableUtilsTest {

    @Test
    void getRequiredVariable() {
        assertNotNull(EnvironmentVariableUtils.getRequiredVariable("TEST_URL"));
    }

}
