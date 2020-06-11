package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
class ParseRequestServiceTest {

    @InjectMocks
    private ParseRequestService sut = new ParseRequestService();

    @Test
    void parseRequest() throws IOException {
        AssignmentRequest assignmentRequest =  TestDataBuilder.buildAssignmentRequest();
        Boolean result = sut.parseRequest(assignmentRequest);
        assertEquals(true, result);
    }

}
