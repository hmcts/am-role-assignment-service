package uk.gov.hmcts.reform.roleassignment.launchdarkly;

import java.io.IOException;

import com.launchdarkly.sdk.LDUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.util.Constants;

@Component
public class LdFlagChecker {

    @Autowired
    private LaunchDarklyConfiguration configuration;

    public boolean verifyServiceAndFlag(String serviceName, String flagName) throws IOException {
        LDUser user = new LDUser.Builder("accessmanagement@hmcts.net")
            .firstName("david")
            .lastName("jones")
            .key(serviceName)
            .build();

        boolean showFeature = configuration.ldClient().boolVariation(flagName, user, false);

        configuration.ldClient().close();
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(Constants.ENDPOINT_NOT_AVAILABLE);
        return showFeature;
    }
}
