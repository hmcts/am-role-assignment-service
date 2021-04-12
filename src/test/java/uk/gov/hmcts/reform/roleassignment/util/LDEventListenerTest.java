package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureFlagListener;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class LDEventListenerTest {


    @Mock
    FeatureFlagListener featureFlagListener = mock(FeatureFlagListener.class);

    @Mock
    FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

    @InjectMocks
    LDEventListener ldEventListener = new LDEventListener();

    @Test
    public void executeListener() throws Exception {

        when(featureToggleService.isFlagEnabled(any())).thenReturn(true);
        doNothing().when(featureFlagListener).logWheneverOneFlagChangesForOneUser(any(), any());
        ldEventListener.setFeatureToggleService(featureToggleService);
        ldEventListener.setFeatureFlagListener(featureFlagListener);
        ldEventListener.run("dsf");
        Mockito.verify(featureToggleService, times(3)).isFlagEnabled(any());
        Mockito.verify(featureFlagListener, times(3)).logWheneverOneFlagChangesForOneUser(any(), any());

    }

    @Test
    public void getFlagsMap() throws Exception {

        Map<String, Boolean> ldEventListenerDroolFlagStates =  ldEventListener.getDroolFlagStates();
        assertNotNull(ldEventListenerDroolFlagStates);
        assertEquals(0, ldEventListenerDroolFlagStates.size());


    }


}
