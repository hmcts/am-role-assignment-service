package uk.gov.hmcts.reform.roleassignment.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.io.IOException;

class JwtGrantedAuthoritiesConverterTest {

    @Mock
    private final IdamRepository idamRepository = mock(IdamRepository.class);

    JwtGrantedAuthoritiesConverter jwtConverter = new JwtGrantedAuthoritiesConverter(idamRepository);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void convert() {
        //jwtConverter.convert(TestDataBuilder.buildJwt());
    }

    @Test
    void getUserInfo() {
    }
}
