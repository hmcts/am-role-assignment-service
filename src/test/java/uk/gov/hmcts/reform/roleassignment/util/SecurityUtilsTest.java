package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.oidc.JwtGrantedAuthoritiesConverter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.SERVICE_AUTHORIZATION;


class SecurityUtilsTest {

    @Mock
    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);

    @Mock
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        mock(JwtGrantedAuthoritiesConverter.class);

    @Mock
    Authentication authentication = Mockito.mock(Authentication.class);

    @Mock
    SecurityContext securityContext = mock(SecurityContext.class);

    @InjectMocks
    private final SecurityUtils securityUtils = new SecurityUtils(
        authTokenGenerator,
        jwtGrantedAuthoritiesConverter
    );

    private final String serviceAuthorization = "auth";
    private static final String USER_ID = "userId";

    ServiceAndUserDetails serviceAndUserDetails;


    private void mockSecurityContextData() throws IOException {
        List<String> collection = new ArrayList<String>();
        collection.add("string");
        serviceAndUserDetails = new ServiceAndUserDetails(
            USER_ID,
            serviceAuthorization,
            collection,
            "servicename"
        );
        Map<String, Object> headers = new HashMap<>();
        headers.put("header", "head");
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(10),headers, headers);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(jwt);
        when(jwtGrantedAuthoritiesConverter.getUserInfo())
            .thenReturn(TestDataBuilder.buildUserInfo("21334a2b-79ce-44eb-9168-2d49a744be9c"));
    }

    @BeforeEach
    public void setUp() throws IOException {
        mockSecurityContextData();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getUserId() {
        assertNotNull(securityUtils.getUserId());
    }

    @Test
    void getUserRoles() {
        assertNotNull(securityUtils.getUserRoles());
    }

    @Test
    void getUserRolesHeader() {
        assertNotNull(securityUtils.getUserRolesHeader());
    }

    @Test
    void getAuthorizationHeaders() {
        assertNotNull(securityUtils.authorizationHeaders());
    }

    @Test
    void removeBearerFromToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SERVICE_AUTHORIZATION,
                          "Bearer eyJhbGciOiJIUzUxMiJ9" +
                              ".eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTQ2ODQ5MTF9" +
                              ".LH3aiNniHNMlTwuSdzgRic9sD_4inQv5oUqJ0kkRKVasS4RfhIz2tRdttf" +
                              "-sSMkUga1p1teOt2iCq4BQBDS7KA");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        assertEquals("ccd_gw", securityUtils.getServiceName());
    }

}
