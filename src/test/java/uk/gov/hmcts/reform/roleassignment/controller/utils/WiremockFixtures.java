package uk.gov.hmcts.reform.roleassignment.controller.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static uk.gov.hmcts.reform.roleassignment.controller.BaseTest.WIRE_MOCK_SERVER;
import static uk.gov.hmcts.reform.roleassignment.util.KeyGenerator.getRsaJwk;

public class WiremockFixtures {

    public static final ObjectMapper OBJECT_MAPPER = new Jackson2ObjectMapperBuilder()
            .modules(new Jdk8Module(), new JavaTimeModule())
            .build();

    public WiremockFixtures() {
        configureFor(WIRE_MOCK_SERVER.port());
    }

    public void resetRequests() {
        resetAllRequests();
    }

    public void stubIdamConfig() throws JsonProcessingException {

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/o/.well-known/openid-configuration"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(OBJECT_MAPPER.writeValueAsString(getOpenIdResponse()))
                ));

        WIRE_MOCK_SERVER.stubFor(get(urlPathMatching("/o/jwks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getJwksResponse())
                ));

    }

    private Map<String, Object> getOpenIdResponse() {
        LinkedHashMap<String,Object> data1 = new LinkedHashMap<>();
        data1.put("issuer", "http://localhost:" + WIRE_MOCK_SERVER.port() + "/o");
        data1.put("jwks_uri", "http://localhost:" + WIRE_MOCK_SERVER.port() + "/o/jwks");

        return data1;
    }

    private String getJwksResponse() {
        try {
            return "{"
                    + "\"keys\": [" + getRsaJwk().toPublicJWK().toJSONString() + "]"
                    + "}";

        } catch (JOSEException ex) {
            throw new RuntimeException(ex);
        }

    }

}
