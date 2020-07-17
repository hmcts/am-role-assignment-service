package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(MockitoJUnitRunner.class)
class IdamRoleServiceTest {

    @Mock
    SecurityUtils securityUtils;

    @InjectMocks
    IdamRoleService idamRoleService = new IdamRoleService();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getUserId() {
        when(securityUtils.getUserId()).thenReturn("6b36bfc6-bb21-11ea-b3de-0242ac140004");
        String userId = idamRoleService.getRequestedUserId();
        assertEquals("6b36bfc6-bb21-11ea-b3de-0242ac140004", userId);
    }

    @Test
    void getUserRole() {
        when(securityUtils.getUserRolesHeader()).thenReturn("SomeRole");
        String userRole = idamRoleService.getRequestUserRole();
        assertEquals("SomeRole", userRole);
    }

    @Test
    void getIdamRoleAssignmentsForActor() throws Exception {
        Collection<RoleAssignment> roleList = new ArrayList<>();
        Collection<RoleAssignment> result = idamRoleService
            .getIdamRoleAssignmentsForActor("6b36bfc6-bb21-11ea-b3de-0242ac140004");
        assertEquals(roleList, result);
    }
}
