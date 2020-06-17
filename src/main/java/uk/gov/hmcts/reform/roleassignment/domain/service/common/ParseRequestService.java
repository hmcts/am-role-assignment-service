package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.CorrelationInterceptorUtil;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

@Service
public class ParseRequestService {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private CorrelationInterceptorUtil correlationInterceptorUtil;

    private String serviceId;
    private String userId;

    public AssignmentRequest parseRequest(AssignmentRequest assignmentRequest) throws Exception {
        //1. validate request and assignment record
        ValidationUtil.validateRoleRequest(assignmentRequest.getRequest());
        ValidationUtil.validateRequestedRoles(assignmentRequest.getRequestedRoles());

        //2. Request Parsing
        //a. Extract client Id and place in the request
        assignmentRequest.getRequest().setClientId(securityUtils.getServiceId());
        //b. Extract AuthenticatedUser Id from the User token and place in the request.
        assignmentRequest.getRequest().setAuthenticatedUserId(UUID.fromString(securityUtils.getUserId()));
        //c. Set Status=Created and created Time = now
        assignmentRequest.getRequest().setStatus(Status.CREATED);
        assignmentRequest.getRequest().setCreated(LocalDateTime.now());
        //d. correlationId if it is empty then generate a new value and set.
        setCorrelationId(assignmentRequest);
        //3. RoleAssignment Parsing
        //a. Copy process and reference from the request to RoleAssignment
        assignmentRequest.getRequestedRoles().forEach(ra -> ra.setReference(assignmentRequest.getRequest().getReference()));
        assignmentRequest.getRequestedRoles().forEach(ra -> ra.setProcess(assignmentRequest.getRequest().getProcess()));
        //b. Set Status=Created and statusSequenceNumber from Status Enum
        assignmentRequest.getRequestedRoles().forEach(ra -> ra.setStatus(Status.CREATED));
        assignmentRequest.getRequestedRoles().forEach(ra -> ra.setStatusSequence(Status.CREATED.sequence));
        //c. created Time = now
        assignmentRequest.getRequestedRoles().forEach(ra -> ra.setCreated(LocalDateTime.now()));

        return assignmentRequest;
    }

    private void setCorrelationId(AssignmentRequest assignmentRequest) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();
        assignmentRequest.getRequest().setCorrelationId(correlationInterceptorUtil.preHandle(request));
        correlationInterceptorUtil.afterCompletion();
    }
}
