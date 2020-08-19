package uk.gov.hmcts.reform.roleassignment;

import java.io.IOException;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import liquibase.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

@Slf4j
public class FeatureFlagToggleEvaluator implements TestRule {

    private final SmokeTest smokeTest;

    public FeatureFlagToggleEvaluator(SmokeTest smokeTest) {
        this.smokeTest = smokeTest;
    }

    @SneakyThrows
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                boolean isFlagEnabled = false;
                FeatureFlagToggle featureFlagToggle = description
                    .getAnnotation(FeatureFlagToggle.class);
                if (featureFlagToggle != null) {
                    if (StringUtils.isNotEmpty(featureFlagToggle.value())) {
                        try (LDClient client = new LDClient(smokeTest.getSdkKey())) {

                            LDUser user = new LDUser.Builder(smokeTest.getEnvironment())
                                .firstName(smokeTest.getUserName())
                                .lastName("user")
                                .custom("servicename", "am_role_assignment_service")
                                .build();

                            isFlagEnabled = client.boolVariation(featureFlagToggle.value(), user, false);
                        } catch (IOException exception) {
                            log.warn("Error getting Launch Darkly connection in Smoke tests");
                        }
                    }

                    Assume.assumeTrue("Test is ignored!", isFlagEnabled);
                    base.evaluate();
                }
            }
        };
    }
}
