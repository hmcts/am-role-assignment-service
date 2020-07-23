package uk.gov.hmcts.reform.roleassignmentrefactored.domain.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignmentrefactored.oidc.IdamRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
class IdamRoleServiceTest {

    @Mock
    private IdamRepository idamRepositoryMock = mock(IdamRepository.class);

    @InjectMocks
    private IdamRoleService sut = new IdamRoleService(idamRepositoryMock);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void getUserRoles() throws IOException {
        String userId = "003352d0-e699-48bc-b6f5-5810411e60af";


        UserDetails userDetails = UserDetails.builder().email("black@betty.com").forename("ram").surname("jam").id(
            "1234567890123456")
            .roles(null).build();

        when(idamRepositoryMock.getUserByUserId(any(),any())).thenReturn(userDetails);

        assertNotNull(sut.getUserRoles(userId));
    }
}
