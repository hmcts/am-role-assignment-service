package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.oidc.JwtGrantedAuthoritiesConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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


    private void mockSecurityContextData() throws IOException {
        List<String> collection = new ArrayList<String>();
        collection.add("string");
        ServiceAndUserDetails serviceAndUserDetails = new ServiceAndUserDetails(
            USER_ID,
            serviceAuthorization,
            collection,
            "servicename"
        );
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(serviceAndUserDetails);

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
        assertNotNull(securityUtils.getUserRoles());
    }
}
