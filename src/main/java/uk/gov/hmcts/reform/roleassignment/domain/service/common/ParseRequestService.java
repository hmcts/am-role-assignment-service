package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.CorrelationInterceptorUtil;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Service
public class ParseRequestService {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private CorrelationInterceptorUtil correlationInterceptorUtil;

    private String serviceId;
    private String userId;

    public AssignmentRequest parseRequest(AssignmentRequest assignmentRequest) throws Exception {
        Request request = assignmentRequest.getRequest();
        //1. validate request and assignment record
        ValidationUtil.validateAssignmentRequest(assignmentRequest);

        //2. Request Parsing
        //a. Extract client Id and place in the request
        request.setClientId(securityUtils.getServiceId());
        //b. Extract AuthenticatedUser Id from the User token and place in the request.
        request.setAuthenticatedUserId(UUID.fromString(securityUtils.getUserId()));
        //c. Set Status=Created and created Time = now
        request.setStatus(Status.CREATED);
        request.setRequestType(RequestType.CREATE);
        request.setCreated(LocalDateTime.now());
        //d. correlationId if it is empty then generate a new value and set.
        setCorrelationId(request);
        //3. RoleAssignment Parsing
        //a. Copy process and reference from the request to RoleAssignment
        //b. Set Status=Created and statusSequenceNumber from Status Enum
        //c. created Time = now
        Collection<RequestedRole> requestedRoles = assignmentRequest.getRequestedRoles();

        requestedRoles.forEach(requestedRole -> {
            requestedRole.setProcess(request.getProcess());
            requestedRole.setReference(request.getReference());
            requestedRole.setStatus(Status.CREATED);
            requestedRole.setStatusSequence(Status.CREATED.sequence);
            requestedRole.setCreated(LocalDateTime.now());

        });
        AssignmentRequest parsedRequest = new AssignmentRequest();
        parsedRequest.setRequest(request);
        parsedRequest.setRequestedRoles(requestedRoles);
        return parsedRequest;
    }

    private void setCorrelationId(Request request) throws Exception {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();
        request.setCorrelationId(correlationInterceptorUtil.preHandle(httpServletRequest));
    }

    public void removeCorrelationLog() throws Exception {
        correlationInterceptorUtil.afterCompletion();
    }
}
