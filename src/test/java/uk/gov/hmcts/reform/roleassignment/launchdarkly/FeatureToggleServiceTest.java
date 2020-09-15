package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeatureToggleServiceTest {

    @Mock
    LDClient ldClient = mock(LDClient.class);

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
}
