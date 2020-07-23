package uk.gov.hmcts.reform.roleassignmentrefactored.data.roleassignment;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentrefactored.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignmentrefactored.data.HistoryEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
class HistoryEntityTest {

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void test() throws IOException {
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryEntity(
            TestDataBuilder.buildRoleAssignment(Status.LIVE),
            TestDataBuilder.buildRequestEntity(TestDataBuilder.buildRequest(Status.APPROVED, false)));

        assertNotNull(historyEntity.getRequestId());
        assertEquals("ab4e8c21-27a0-4abd-aed8-810fdce22adb", historyEntity.getRequestId().toString());
    }


}
