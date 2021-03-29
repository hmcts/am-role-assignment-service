package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureToggleService.SERVICE_NAME;

@Service
public class FeatureFlagListener {

    @Autowired
    private final LDClient ldClient;

    public FeatureFlagListener(LDClient ldClient) {
        this.ldClient = ldClient;
    }


    public void logWheneverAnyFlagChanges() {
        ldClient.getFlagTracker().addFlagChangeListener(event -> {
            System.out.printf("Flag \"%s\" has changed\n", event.getKey());
        });
    }





   public void logWheneverOneFlagChangesForOneUser( String flagKey, LDUser user) {
        System.out.println("Inside method");
        if(ldClient !=null) {
            ldClient.getFlagTracker().addFlagValueChangeListener(flagKey, user, event -> {
                System.out.printf("Flag \"%s\" for user \"%s\" has changed from %s to %s\n", event.getKey(),
                                  user.getKey(), event.getOldValue(), event.getNewValue()
                );
                if(event.getNewValue() != event.getOldValue()){
                    System.out.println("Value inserted into db");
                }
            });
        }
    }
}
