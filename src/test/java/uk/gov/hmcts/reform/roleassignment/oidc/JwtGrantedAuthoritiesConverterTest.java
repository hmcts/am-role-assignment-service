package uk.gov.hmcts.reform.roleassignment.oidc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


class JwtGrantedAuthoritiesConverterTest {

    @Mock
    private IdamRepository idamRepositoryMock = mock(IdamRepository.class);

    private UserInfo userInfo;

    @InjectMocks
    private JwtGrantedAuthoritiesConverter sut = new JwtGrantedAuthoritiesConverter(idamRepositoryMock);

    @Test
    @DisplayName("Gets empty authorities")
    void shouldReturnEmptyAuthorities() {
        Jwt jwt = Mockito.mock(Jwt.class);
        Collection<GrantedAuthority> authorities = sut.convert(jwt);
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    @DisplayName("No Claims should return empty authorities")
    void shouldReturnEmptyAuthoritiesWhenClaimNotAvailable() {
        Jwt jwt = Mockito.mock(Jwt.class);
        when(jwt.containsClaim(anyString())).thenReturn(false);
        Collection<GrantedAuthority> authorities = sut.convert(jwt);
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    @DisplayName("Should return empty authorities when token value is not matching with expected")
    void shouldReturnEmptyAuthoritiesWhenClaimValueNotEquals() {
        Jwt jwt = Mockito.mock(Jwt.class);
        when(jwt.containsClaim(anyString())).thenReturn(true);
        when(jwt.getClaim(anyString())).thenReturn("Test");
        Collection<GrantedAuthority> authorities = sut.convert(jwt);
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    @DisplayName("Should return empty authorities when token value is not matching with expected")
    void shouldReturnEmptyAuthoritiesWhenIdamReturnsNoUsers() {
        Jwt jwt = Mockito.mock(Jwt.class);
        when(jwt.containsClaim(anyString())).thenReturn(true);
        when(jwt.getClaim(anyString())).thenReturn("access_token");
        when(jwt.getTokenValue()).thenReturn("access_token");
        UserInfo userInfo = mock(UserInfo.class);
        List<String> roles = Arrays.asList();
        when(userInfo.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfo);
        Collection<GrantedAuthority> authorities = sut.convert(jwt);
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    @DisplayName("Should return authorities when token value is matching with expected")
    void shouldReturnAuthoritiesWhenIdamReturnsUserRoles() {
        Jwt jwt = Mockito.mock(Jwt.class);
        when(jwt.containsClaim(anyString())).thenReturn(true);
        when(jwt.getClaim(anyString())).thenReturn("access_token");
        when(jwt.getTokenValue()).thenReturn("access_token");
        UserInfo userInfo = mock(UserInfo.class);
        List<String> roles = Arrays.asList("citizen");
        when(userInfo.getRoles()).thenReturn(roles);
        when(userInfo.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfo);
        Collection<GrantedAuthority> authorities = sut.convert(jwt);
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
    }

    public static Jwt buildJwt(String tokenName) {
        return Jwt.withTokenValue("token_value").header("head", "head")
            .claim(tokenName, ACCESS_TOKEN).build();
    }
}
