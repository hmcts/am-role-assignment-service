package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;

class EnvironmentVariableUtilsTest {

    @Test
    void getRequiredVariable() {
        Assertions.assertNotNull(EnvironmentVariableUtils.getRequiredVariable("PATH"));

    }

    @Test
    void getNonExistingRequiredVariable() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            EnvironmentVariableUtils.getRequiredVariable("A_DUMMY_VARIABLE");
        });
    }

}
