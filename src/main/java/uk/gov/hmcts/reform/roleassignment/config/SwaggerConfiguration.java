package uk.gov.hmcts.reform.roleassignment.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.SERVICE_AUTHORIZATION2;

@Configuration
public class SwaggerConfiguration {

    private static final String DESCRIPTION = "API manages the assignment of roles with attributes to actors, to"
        + " support both ccd access control and work allocation requirements.";

    @Value("${swaggerUrl}")
    private String host;

    @Bean
    public GroupedOpenApi publicApi(@Autowired OperationCustomizer customGlobalHeaders) {
        return GroupedOpenApi.builder()
            .group("am-role-assignment-service")
            .pathsToMatch("/**")
            .build();
    }

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation customOperation, HandlerMethod handlerMethod) -> {
            Parameter serviceAuthorizationHeader = new Parameter()
                .in(ParameterIn.HEADER.toString())
                .schema(new StringSchema())
                .name(SERVICE_AUTHORIZATION2)
                .description("Valid Service-to-Service JWT token for a whitelisted micro-service")
                .required(true);
            Parameter authorizationHeader = new Parameter()
                .in(ParameterIn.HEADER.toString())
                .schema(new StringSchema())
                .name(AUTHORIZATION)
                .description("Keyword `Bearer` followed by a valid IDAM user token")
                .required(true);
            customOperation.addParametersItem(authorizationHeader);
            customOperation.addParametersItem(serviceAuthorizationHeader);
            return customOperation;
        };
    }

}
