package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
class ParseRequestServiceTest {

    @InjectMocks
    private ParseRequestService sut = new ParseRequestService();

    @Mock
    private SecurityUtils securityUtilsMock = mock(SecurityUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parseRequest() throws IOException {
        AssignmentRequest assignmentRequest =  TestDataBuilder.buildAssignmentRequest();

        when(securityUtilsMock.getServiceId()).thenReturn("copied client id");
        when(securityUtilsMock.getUserId()).thenReturn("21334a2b-79ce-44eb-9168-2d49a744be9c");

        AssignmentRequest result = sut.parseRequest(assignmentRequest);
        assertNotNull(result);
    }

}
