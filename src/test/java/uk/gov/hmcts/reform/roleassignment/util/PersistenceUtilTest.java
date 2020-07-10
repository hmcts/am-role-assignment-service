package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
class PersistenceUtilTest {

    @InjectMocks
    PersistenceUtil idamRoleService = new PersistenceUtil();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void convertRoleAssignmentToHistoryEntity() {
        //idamRoleService.convertRoleAssignmentToHistoryEntity();
    }

    @Test
    void convertRequestToEntity() {
        //idamRoleService.convertRequestToEntity();
    }

    @Test
    void convertRoleAssignmentToEntity() {
        //idamRoleService.convertRoleAssignmentToEntity();
    }

    @Test
    void convertActorCacheToEntity() {
        //idamRoleService.convertActorCacheToEntity();
    }

    @Test
    void convertHistoryEntityToRoleAssignment() {
        //idamRoleService.convertHistoryEntityToRoleAssignment();
    }

    @Test
    void convertEntityToRoleAssignment() {
        //idamRoleService.convertEntityToRoleAssignment();
    }
}
