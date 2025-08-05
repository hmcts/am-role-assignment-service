package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;
import uk.gov.hmcts.reform.roleassignment.oidc.OIdcAdminConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdamRoleServiceTest {

    @Mock
    private IdamRepository idamRepositoryMock;

    @Mock
    private OIdcAdminConfiguration oidcAdminConfiguration;

    @InjectMocks
    private IdamRoleService sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getUserRoles() {
        String userId = "003352d0-e699-48bc-b6f5-5810411e60af";

        when(idamRepositoryMock.searchUserByUserId(any(), any()))
            .thenReturn(ResponseEntity.ok().body(new ArrayList<>() {
                {
                    add(new LinkedHashMap<String, Object>() {
                            {
                                put("id", "someId");
                                put("roles", new ArrayList<String>() {
                                    {
                                        add("role1");
                                        add("role2");
                                    }
                                });
                            }
                        }
                    );
                }
            }));

        assertNotNull(sut.getUserRoles(userId));
    }

    @Test
    void getUserRolesEmptyUserDetails() {
        String userId = "003352d0-e699-48bc-b6f5-5810411e60af";

        when(idamRepositoryMock.searchUserByUserId(any(), any()))
            .thenReturn(ResponseEntity.ok().body(new ArrayList<>()));

        assertNotNull(sut.getUserRoles(userId));
    }

    @Test
    void getUserRolesBlankResponse() {
        String userId = "003352d0-e699-48bc-b6f5-5810411e60af";
        assertNotNull(sut.getUserRoles(userId));
    }

}
