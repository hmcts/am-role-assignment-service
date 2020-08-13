package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.jackson.JacksonEncoder;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.util.UserTokenProviderConfig;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    protected static final ObjectMapper mapper = new ObjectMapper();

    protected static final MediaType JSON_CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        StandardCharsets.UTF_8);


    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    protected DataSource db;

    RestTemplate restTemplate = new RestTemplate();

    @BeforeClass
    public static void init() {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public ServiceAuthorisationApi generateServiceAuthorisationApi(final String s2sUrl) {
        return Feign.builder()
            .encoder(new JacksonEncoder())
            .contract(new SpringMvcContract())
            .target(ServiceAuthorisationApi.class, s2sUrl);
    }

    public ServiceAuthTokenGenerator authTokenGenerator(
        final String secret,
        final String microService,
        final ServiceAuthorisationApi serviceAuthorisationApi) {
        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

    public String searchUserByUserId(UserTokenProviderConfig config) {
        TokenRequest request = config.prepareTokenRequest();
        ResponseEntity<TokenResponse> response = new ResponseEntity<>(HttpStatus.OK);
        HttpHeaders headers = new HttpHeaders();
        try {
            String url = String.format(
                "%s/o/token?client_id=%s&client_secret=%s&grant_type=%s&scope=%s&username=%s&password=%s",
                config.getIdamURL(),
                request.getClientId(),
                request.getClientSecret(),
                request.getGrantType(),
                request.getScope(),
                request.getUsername(),
                request.getPassword()
            );
            log.info("URL :   {}", url);
            headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
            HttpEntity<?> entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                TokenResponse.class
            );
            if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
                log.info("Positive response");
                return response.getBody().accessToken;
            } else {
                log.error("There is some problem in fetching access token {}", response
                    .getStatusCode());
                throw new ResourceNotFoundException("Not Found");
            }
        } catch (HttpClientErrorException exception) {
            log.error("HttpClientErrorException {}", exception.getMessage());
            throw new BadRequestException("Unable to fetch access token");

        }
    }
}
