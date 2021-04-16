package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.util.LDEventListener;

import java.util.Map;

@Service
@Slf4j
public class FeatureFlagListener {

    @Autowired
    private final LDClient ldClient;

    @Autowired
    private LDEventListener ldEventListener;

    public FeatureFlagListener(LDClient ldClient) {
        this.ldClient = ldClient;
    }


    public void logWheneverOneFlagChangesForOneUser(String flagKey, LDUser user) {
        if (ldClient != null) {
            ldClient.getFlagTracker().addFlagValueChangeListener(flagKey, user, event -> {
                log.info("Flag {} for user {} has changed from {} to {}", event.getKey(),
                                  user.getKey(), event.getOldValue(), event.getNewValue());
                if (event.getNewValue() != event.getOldValue()) {
                    Map<String, Boolean> droolFlagStates = ldEventListener.getDroolFlagStates();
                    droolFlagStates.put(event.getKey(), event.getNewValue().booleanValue());
                }
            });
        }
    }
}
