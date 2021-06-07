package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class FeatureToggleServiceTest {

    @Mock
    LDClient ldClient = mock(LDClient.class);

    @Mock
    HttpServletRequest request  = mock(HttpServletRequest.class);

    @InjectMocks
    FeatureToggleService featureToggleService = new FeatureToggleService(ldClient, "user");

    @Test
    public void evaluateLdFlag() {
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(true);
        featureToggleService = new FeatureToggleService(ldClient, "user");
        Assertions.assertTrue(featureToggleService.isFlagEnabled("serviceName", "userName"));
    }

    @Test
    public void evaluateLdFlagFalse() {
        when(ldClient.boolVariation(any(), any(), anyBoolean())).thenReturn(false);
        featureToggleService = new FeatureToggleService(ldClient, "user");
        Assertions.assertFalse(featureToggleService.isFlagEnabled("serviceName", "userName"));
    }

    @Test
    public void isValidFlag() {
        when(ldClient.isFlagKnown(any())).thenReturn(true);
        featureToggleService = new FeatureToggleService(ldClient, "user");
        Assertions.assertTrue(featureToggleService.isValidFlag("serviceName"));
    }

    @Test
    public void isValidFlagReturnsFalse() {
        when(ldClient.isFlagKnown(any())).thenReturn(false);
        featureToggleService = new FeatureToggleService(ldClient, "user");
        Assertions.assertFalse(featureToggleService.isValidFlag("serviceName"));
    }

    @ParameterizedTest
    @CsvSource({
        "/am/role-assignments/fetchFlagStatus,GET,get-db-drools-flag",
        "/am/role-assignments/createFeatureFlag,GET,get-db-drools-flag"
    })
    void getLdFlagGetCase(String url, String method, String flag) {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        featureToggleService = new FeatureToggleService(ldClient, "user");
        String flagName = featureToggleService.getLaunchDarklyFlag(request);
        Assertions.assertEquals(flag, flagName);
    }

    @ParameterizedTest
    @CsvSource({
        "/am/role-assignments/createFeatureFlag,GET,get-db-drools-flag"
    })
    void getLdFlagGet_RoleAssignmentCase(String url, String method, String flag) {
        when(request.getRequestURI()).thenReturn(url);
        when(request.getMethod()).thenReturn(method);
        featureToggleService = new FeatureToggleService(ldClient, "user");
        String flagName = featureToggleService.getLaunchDarklyFlag(request);
        Assertions.assertEquals(flag, flagName);
    }

    @ParameterizedTest
    @CsvSource({
        "GET",
        "DELETE",
        "POST",
        "POST",
        "INVALID",
    })
    void getLdFlagCase(String method) {
        when(request.getRequestURI()).thenReturn("/am/dummy");
        when(request.getMethod()).thenReturn(method);
        featureToggleService = new FeatureToggleService(ldClient, "user");
        String flagName = featureToggleService.getLaunchDarklyFlag(request);
        Assertions.assertNull(flagName);
    }

    @Test
    @CsvSource({
        "/am/role-assignments/,DELETE,delete-role-assignments-by-id",
    })
    public void getLdFlagDeleteStringContainsCase() {
        when(request.getRequestURI()).thenReturn("/am/role-assignments/");
        when(request.getMethod()).thenReturn("DELETE");
        featureToggleService = new FeatureToggleService(ldClient, "user");
        String flagName = featureToggleService.getLaunchDarklyFlag(request);
        Assertions.assertNull(flagName);
    }
}
