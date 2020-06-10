package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

class ParseRequestServiceTest {

    @InjectMocks
    private ParseRequestService sut = new ParseRequestService();

    @Test
    void parseRequest() {
        AssignmentRequest assignmentRequest =  TestDataBuilder.buildAssignmentRequest();
        sut.parseRequest(assignmentRequest);

    }
}
