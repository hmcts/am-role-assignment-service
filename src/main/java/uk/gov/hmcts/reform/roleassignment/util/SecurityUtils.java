package uk.gov.hmcts.reform.roleassignment.util;

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
import uk.gov.hmcts.reform.roleassignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.roleassignment.oidc.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.roleassignment.util.Constants.BEARER;
import static uk.gov.hmcts.reform.roleassignment.util.Constants.SERVICE_AUTHORIZATION;

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
        final var headers = new HttpHeaders();
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.add("user-id", getUserId());
        headers.add("user-roles", getUserRolesHeader());

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            headers.add(HttpHeaders.AUTHORIZATION, getUserBearerToken());
        }
        return headers;
    }

    String getUserBearerToken() {
        return BEARER + getUserToken();
    }


    public String getUserId() {
        return jwtGrantedAuthoritiesConverter.getUserInfo().getUid();
    }

    public UserRoles getUserRoles() {
        var userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();
        return UserRoles.builder()
            .uid(userInfo.getUid())
            .roles(userInfo.getRoles())
            .build();
    }


    public String getUserToken() {
        var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
        var servletRequestAttributes =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());

        if (servletRequestAttributes != null
            && servletRequestAttributes.getRequest().getHeader(SERVICE_AUTHORIZATION) != null) {
            return JWT.decode(removeBearerFromToken(servletRequestAttributes.getRequest().getHeader(
                SERVICE_AUTHORIZATION))).getSubject();
        }
        return null;
    }

    private String removeBearerFromToken(String token) {
        if (!token.startsWith(BEARER)) {
            return token;
        } else {
            return token.substring(BEARER.length());
        }
    }

    public String getServiceAuthorizationHeader() {
        return authTokenGenerator.generate();
    }

}
