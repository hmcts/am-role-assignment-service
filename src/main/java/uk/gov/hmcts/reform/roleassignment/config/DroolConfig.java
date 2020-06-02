package uk.gov.hmcts.reform.roleassignment.config;

import org.kie.api.KieServices;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class DroolConfig {

    private KieServices  kieServices = KieServices.Factory.get();

    @Bean
    public KieContainer kieContainer() throws IOException {
        return  kieServices.getKieClasspathContainer();
    }

    @Bean
    public StatelessKieSession kieSession() throws IOException {
        return kieContainer().newStatelessKieSession("role-assignment-validation-session");
    }
}
