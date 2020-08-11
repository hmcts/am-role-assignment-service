package uk.gov.hmcts.reform.roleassignment;

import java.io.IOException;
import java.util.Optional;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.LDClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class EvaluateLaunchDarklyFlag implements ExecutionCondition {

    private static final String MICROSERVICE_NAME = "am_role_assignment_service";

    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult
        .enabled("Feature Flag is enabled");

    private static final ConditionEvaluationResult DISABLED = ConditionEvaluationResult
        .disabled("Feature Flag is disabled");

    @Autowired
    ApplicationContext context;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        SmokeTest test = (SmokeTest) context.getTestInstance().get();

        boolean isFlagEnabled = false;

        Optional<LaunchDarklyFlagEvaluator> flagName = AnnotationSupport
            .findAnnotation(context.getElement(), LaunchDarklyFlagEvaluator.class);

        if (flagName.isPresent()) {
            try (LDClient client = new LDClient(test.sdkKey)) {

                LDUser user = new LDUser.Builder(test.environment)
                    .firstName(test.userName)
                    .lastName("user")
                    .custom("servicename", MICROSERVICE_NAME)
                    .build();

                isFlagEnabled = client.boolVariation(flagName.get().value(), user, false);
            } catch (IOException exception) {
                log.warn("Error getting Launch Darkly connection in Smoke tests");
            }
        }

        return isFlagEnabled ? ENABLED : DISABLED;
    }
}
