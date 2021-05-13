package uk.gov.hmcts.reform.roleassignment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.CreateAssignmentController;

import java.util.Arrays;

@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfiguration {


    @Value("${swaggerUrl}")
    private String host;
    private static final String VALUE = "string";
    private static final String HEADER = "header";

    @Bean
    public Docket apiV2() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("v2")
            .select()
            .apis(RequestHandlerSelectors.basePackage(CreateAssignmentController.class.getPackage().getName()))
            .build()
            .useDefaultResponseMessages(false)
            .apiInfo(apiV2Info())
            .host(host)
            .globalOperationParameters(Arrays.asList(
                headerServiceAuthorization(),
                headerAuthorization()

            ));
    }

    private ApiInfo apiV2Info() {
        return new ApiInfoBuilder()
            .title("Role Assignment Service")
            .description("Manage role assignments")
            .version("2-beta")
            .build();
    }

    private Parameter headerServiceAuthorization() {
        return new ParameterBuilder()
            .name("ServiceAuthorization")
            .description("Valid Service-to-Service JWT token for a whitelisted micro-service")
            .modelRef(new ModelRef(VALUE))
            .parameterType(HEADER)
            .required(true)
            .build();
    }

    private Parameter headerAuthorization() {
        return new ParameterBuilder()
            .name("Authorization")
            .description("Keyword `Bearer` followed by a valid IDAM user token")
            .modelRef(new ModelRef(VALUE))
            .parameterType(HEADER)
            .required(true)
            .build();
    }




}
