package uk.gov.hmcts.reform.roleassignment.util;

import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;


class SecurityUtilsTest {

    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
    private final ServiceAuthorisationApi serviceAuthorisationApi = mock(ServiceAuthorisationApi.class);
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
        mock(JwtGrantedAuthoritiesConverter.class);

    private final SecurityUtils securityUtils = new SecurityUtils(
        authTokenGenerator,
        jwtGrantedAuthoritiesConverter,
        serviceAuthorisationApi
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
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(serviceAndUserDetails);
    }


}
