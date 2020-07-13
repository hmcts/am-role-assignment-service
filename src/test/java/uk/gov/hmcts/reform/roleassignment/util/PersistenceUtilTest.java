package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
class PersistenceUtilTest {

    @InjectMocks
    PersistenceUtil persistenceUtil = new PersistenceUtil();


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void convertRoleAssignmentToHistoryEntity() throws IOException {
        assertNotNull(persistenceUtil
                          .convertRoleAssignmentToHistoryEntity(
                              TestDataBuilder.buildRequestedRole(),
                              persistenceUtil.convertRequestToEntity(TestDataBuilder.buildRequest()))
        );
    }

    @Test
    void convertRequestToEntity() {
        assertNotNull(persistenceUtil.convertRequestToEntity(TestDataBuilder.buildRequest()));
    }

    @Test
    void convertRoleAssignmentToEntity() throws IOException {
        assertNotNull(persistenceUtil.convertRoleAssignmentToEntity(TestDataBuilder.buildRequestedRole()));
    }

    @Test
    void convertActorCacheToEntity() throws IOException {
        assertNotNull(persistenceUtil.convertActorCacheToEntity(TestDataBuilder.buildActorCache()));
    }

    @Test
    void convertHistoryEntityToRoleAssignment() {
        //persistenceUtil.convertHistoryEntityToRoleAssignment();
    }

    @Test
    void convertEntityToRoleAssignment() {
        //persistenceUtil.convertEntityToRoleAssignment();
    }
}
