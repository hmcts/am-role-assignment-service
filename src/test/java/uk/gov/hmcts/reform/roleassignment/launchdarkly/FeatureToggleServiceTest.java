package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
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
public class FeatureToggleServiceTest {

    @Mock
    LDClient ldClient = mock(LDClient.class);

    @Mock
    HttpServletRequest request;

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

    @Test
    public void getLdFlagGetCase() {
        when(request.getRequestURI()).thenReturn("/am/role-assignments/ld/endpoint");
        when(request.getMethod()).thenReturn("GET");
        featureToggleService = new FeatureToggleService(ldClient, "user");
        String flagName = featureToggleService.getLaunchDarklyFlag(request);
        Assertions.assertEquals("get-ld-flag", flagName);
    }

    @Test
    public void getLdFlagPostCase() {
        when(request.getRequestURI()).thenReturn("/am/role-assignments");
        when(request.getMethod()).thenReturn("POST");
        featureToggleService = new FeatureToggleService(ldClient, "user");
        String flagName = featureToggleService.getLaunchDarklyFlag(request);
        Assertions.assertEquals("create-role-assignments", flagName);
    }

    @Test
    public void getLdFlagDeleteCase() {
        when(request.getRequestURI()).thenReturn("/am/role-assignments");
        when(request.getMethod()).thenReturn("DELETE");
        featureToggleService = new FeatureToggleService(ldClient, "user");
        String flagName = featureToggleService.getLaunchDarklyFlag(request);
        Assertions.assertEquals("delete-role-assignments", flagName);
    }

    @Test
    public void getLdFlagNoneMatchCase() {
        when(request.getRequestURI()).thenReturn("/am/dummy");
        when(request.getMethod()).thenReturn("POST");
        featureToggleService = new FeatureToggleService(ldClient, "user");
        String flagName = featureToggleService.getLaunchDarklyFlag(request);
        Assertions.assertNull(flagName);
    }
}
