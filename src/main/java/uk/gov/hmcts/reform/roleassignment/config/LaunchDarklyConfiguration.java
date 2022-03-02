package uk.gov.hmcts.reform.roleassignment.config;

import com.launchdarkly.sdk.server.LDClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureConditionEvaluation;

@Configuration
@ConditionalOnProperty(prefix = "launchdarklyOnStartUp", name = "runOnStartup", havingValue = "false")
public class LaunchDarklyConfiguration implements WebMvcConfigurer {

    @Bean
    public LDClient ldClient(@Value("${LD_SDK_KEY:}") String sdkKey) {
        return new LDClient(sdkKey);
    }

    @Autowired
    private FeatureConditionEvaluation featureConditionEvaluation;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //Any new end point need to be placed in respective map.
        //registry.addInterceptor(featureConditionEvaluation).addPathPatterns("/am/role-assignments")
        registry.addInterceptor(featureConditionEvaluation).addPathPatterns("/am/role-assignments/createFeatureFlag");
        registry.addInterceptor(featureConditionEvaluation).addPathPatterns("/am/role-assignments/fetchFlagStatus");
        registry.addInterceptor(featureConditionEvaluation).addPathPatterns("/am/role-assignments/query/delete");
    }
}

