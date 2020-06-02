package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;

@Service
public class ParseRequestService {
    //1. Validate incoming data
    //2. Mapping to model objects

    public boolean parseRequest(RoleAssignmentRequest roleAssignmentRequest) {

        return Boolean.TRUE;

    }
}
