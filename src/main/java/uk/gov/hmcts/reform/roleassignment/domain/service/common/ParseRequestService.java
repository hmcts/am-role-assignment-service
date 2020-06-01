package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;

@Service
public class ParseRequestService {

    //This is going first service to receive and validate payload
    //This is plcaholder can be use if required

    public boolean parseRequest(RoleAssignmentRequest roleAssignmentRequest) {

        return Boolean.TRUE;

    }
}
