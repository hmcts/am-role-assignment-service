package uk.gov.hmcts.reform.roleassignmentrefactored.util;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.roleassignmentrefactored.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.roleassignmentrefactored.util.SecurityUtils;

import java.util.ArrayList;
import java.util.List;

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


    private void mockSecurityContextData() {
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
    }

    @BeforeEach
    public void setUp() {
        mockSecurityContextData();
        MockitoAnnotations.initMocks(this);
    }
}
