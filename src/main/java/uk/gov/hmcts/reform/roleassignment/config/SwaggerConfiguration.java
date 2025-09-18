package uk.gov.hmcts.reform.roleassignment.config;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
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
@SecurityScheme(name = AUTHORIZATION, type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer",
    description = "Valid IDAM user token, (Bearer keyword is added automatically)")
@SecurityScheme(name = SERVICE_AUTHORIZATION2, type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER, bearerFormat = "JWT",
    description = SwaggerConfiguration.HEADER_S2S_DESCRIPTION)
public class SwaggerConfiguration {

    private static final String DESCRIPTION = "API manages the assignment of roles with attributes to actors, to"
        + " support both ccd access control and work allocation requirements.";

    protected static final String HEADER_AUTH_DESCRIPTION
        = "Keyword `Bearer` followed by a valid IDAM user token";
    protected static final String HEADER_S2S_DESCRIPTION
        = "Valid Service-to-Service JWT token for a whitelisted micro-service";

    @Value("${swaggerUrl}")
    private String host;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                      .title("AM Role Assignment Service")
                      .description(DESCRIPTION))
            .externalDocs(new ExternalDocumentation()
                              .description("README")
                              .url("https://github.com/hmcts/am-role-assignment-service#readme"));
    }

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
                .description(HEADER_S2S_DESCRIPTION)
                .required(true);
            Parameter authorizationHeader = new Parameter()
                .in(ParameterIn.HEADER.toString())
                .schema(new StringSchema())
                .name(AUTHORIZATION)
                .description(HEADER_AUTH_DESCRIPTION)
                .required(true);
            customOperation.addParametersItem(authorizationHeader);
            customOperation.addParametersItem(serviceAuthorizationHeader);
            return customOperation;
        };
    }

}
