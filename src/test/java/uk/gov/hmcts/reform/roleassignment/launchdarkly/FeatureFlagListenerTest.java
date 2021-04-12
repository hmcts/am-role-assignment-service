package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.util.LDEventListener;

import javax.inject.Inject;

import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService.SERVICE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class FeatureFlagListenerTest {


    @Mock
    LDEventListener ldEventListener = mock(LDEventListener.class);

    @Inject
    FeatureFlagListener featureFlagListener;

    @Test
    public void logWheneverOneFlagChangesForOneUser() {
        LDUser user = new LDUser.Builder("pr")
            .firstName("ras")
            .lastName("am")
            .custom(SERVICE_NAME, "am_role_assignment_service")
            .build();

        LDClient ldClient = new LDClient("sds");
        featureFlagListener = new FeatureFlagListener(ldClient);

        featureFlagListener.logWheneverOneFlagChangesForOneUser("ras_drool_judicial_flag_1_0", user);
    }
}
