package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.util.LDEventListener;

import java.util.Map;

@Service
public class FeatureFlagListener {

    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagListener.class);

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
                logger.info("Flag \"%s\" for user \"%s\" has changed from %s to %s", event.getKey(),
                                  user.getKey(), event.getOldValue(), event.getNewValue()
                );
                if (event.getNewValue() != event.getOldValue()) {
                    Map<String, Boolean> droolFlagStates = ldEventListener.getDroolFlagStates();
                    droolFlagStates.put(event.getKey(), event.getNewValue().booleanValue());
                }
            });
        }
    }
}
