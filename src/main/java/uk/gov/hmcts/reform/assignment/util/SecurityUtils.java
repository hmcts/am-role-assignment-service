package uk.gov.hmcts.reform.assignment.util;

import com.auth0.jwt.JWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.assignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.assignment.oidc.JwtGrantedAuthoritiesConverter;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.assignment.util.Constants.BEARER;
import static uk.gov.hmcts.reform.assignment.util.Constants.SERVICE_AUTHORIZATION;

@Service
public class SecurityUtils {

    private final AuthTokenGenerator authTokenGenerator;
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;


    @Autowired
    public SecurityUtils(final AuthTokenGenerator authTokenGenerator,
                         JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter
                         ) {
        this.authTokenGenerator = authTokenGenerator;
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;

    }

    public HttpHeaders authorizationHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.add("user-id", getUserId());
        headers.add("user-roles", getUserRolesHeader());

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            headers.add(HttpHeaders.AUTHORIZATION, getUserBearerToken());
        }
        return headers;
    }

    private String getUserBearerToken() {
        return BEARER + getUserToken();
    }


    public String getUserId() {
        return jwtGrantedAuthoritiesConverter.getUserInfo().getUid();
    }

    public UserRoles getUserRoles() {
        UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
        return UserRoles.builder()
            .uid(userInfo.getUid())
            .roles(userInfo.getRoles())
            .build();
    }


    public String getUserToken() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getTokenValue();
    }

    public String getUserRolesHeader() {
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext()
            .getAuthentication().getAuthorities();
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
    }


    public String getServiceName() {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        return JWT.decode(removeBearerFromToken(request.getHeader(SERVICE_AUTHORIZATION))).getSubject();
    }

    private String removeBearerFromToken(String token) {
        if (!token.startsWith(BEARER)) {
            return token;
        } else {
            return token.substring(BEARER.length());
        }
    }
}
