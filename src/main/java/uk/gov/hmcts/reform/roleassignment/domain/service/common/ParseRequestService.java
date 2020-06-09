package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;

@Service
public class ParseRequestService {
    //1. Validate incoming data
    //2. Mapping to model objects

    public boolean parseRequest(AssignmentRequest assignmentRequest) {

        return Boolean.TRUE;

    }
}
