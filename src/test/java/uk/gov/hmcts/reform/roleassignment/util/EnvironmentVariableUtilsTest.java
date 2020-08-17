package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnvironmentVariableUtilsTest {

    @Test
    void getRequiredVariable() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            EnvironmentVariableUtils.getRequiredVariable("A_DUMMY_VARIABLE");
        });

    }
}
