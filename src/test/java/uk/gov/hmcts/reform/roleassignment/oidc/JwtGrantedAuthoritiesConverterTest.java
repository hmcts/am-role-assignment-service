package uk.gov.hmcts.reform.roleassignment.oidc;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;


class JwtGrantedAuthoritiesConverterTest {

    @Mock
    private IdamRepository idamRepositoryMock = mock(IdamRepository.class);

    private UserInfo userInfo;

    @InjectMocks
    private JwtGrantedAuthoritiesConverter sut = new JwtGrantedAuthoritiesConverter(idamRepositoryMock);

    @Test
    void shouldGetUserInfo() throws Exception {
        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9c";
        Jwt jwt = buildJwt("tokenName");
        when(idamRepositoryMock.getUserInfo(jwt.getTokenValue()))
            .thenReturn(TestDataBuilder.buildUserInfo(userId));
        Collection<GrantedAuthority> result = sut.convert(jwt);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldNotGetUserInfo() throws Exception {
        String userId = "21334a2b-79ce-44eb-9168-2d49a744be9c";
        Jwt jwt = buildJwt("tokenName1");
        when(idamRepositoryMock.getUserInfo(jwt.getTokenValue()))
            .thenReturn(TestDataBuilder.buildUserInfo(userId));
        Collection<GrantedAuthority> result = sut.convert(jwt);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    public static Jwt buildJwt(String tokenName) {
        return Jwt.withTokenValue("token_value").header("head", "head")
            .claim(tokenName, ACCESS_TOKEN).build();
    }
}
