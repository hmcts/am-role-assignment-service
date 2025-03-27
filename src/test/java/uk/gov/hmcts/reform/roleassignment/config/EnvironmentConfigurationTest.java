package uk.gov.hmcts.reform.roleassignment.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentConfigurationTest {

    @Test
    void testGetEnvironment_shouldReturnLaunchDarklyValueWhenBothSupplied() {

        // GIVEN
        String launchDarklyEnvironment = "launchDarklyEnvironment";
        String rasEnvironment = "rasEnvironment";

        // WHEN
        var cut = new EnvironmentConfiguration(launchDarklyEnvironment, rasEnvironment);

        // THEN
        assertEquals(launchDarklyEnvironment, cut.getEnvironment());

    }

    @ParameterizedTest
    @NullAndEmptySource
    void testGetEnvironment_shouldReturnRasValueWhenLaunchDarklyValueNotSupplied(String launchDarklyEnvironment) {

        // GIVEN
        String rasEnvironment = "rasEnvironment";

        // WHEN
        var cut = new EnvironmentConfiguration(launchDarklyEnvironment, rasEnvironment);

        // THEN
        assertEquals(rasEnvironment, cut.getEnvironment());

    }

}
