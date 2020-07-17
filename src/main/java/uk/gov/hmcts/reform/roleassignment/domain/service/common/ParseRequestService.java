package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.CorrelationInterceptorUtil;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service
public class ParseRequestService {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private CorrelationInterceptorUtil correlationInterceptorUtil;

    public AssignmentRequest parseRequest(AssignmentRequest assignmentRequest, RequestType requestType)
        throws ParseException {
        Request request = assignmentRequest.getRequest();
        //1. validates request and assignment record
        ValidationUtil.validateAssignmentRequest(assignmentRequest);

        //2. Request Parsing
        //a. Extract client Id and place in the request
        request.setClientId(securityUtils.getServiceId());
        //b. Extract AuthenticatedUser Id from the User token and place in the request.
        request.setAuthenticatedUserId(UUID.fromString(securityUtils.getUserId()));
        //c. Set Status=Created and created Time = now
        request.setStatus(Status.CREATED);
        request.setRequestType(requestType);
        request.setCreated(LocalDateTime.now());
        //d. correlationId if it is empty then generate a new value and set.
        setCorrelationId(request);
        //3. RoleAssignment Parsing
        //a. Copy process and reference from the request to RoleAssignment
        //b. Set Status=Created and statusSequenceNumber from Status Enum
        //c. created Time = now
        Collection<RoleAssignment> requestedAssignments = assignmentRequest.getRequestedRoles();

        requestedAssignments.forEach(requestedAssignment -> {
            requestedAssignment.setProcess(request.getProcess());
            requestedAssignment.setReference(request.getReference());
            requestedAssignment.setStatus(Status.CREATED);
            requestedAssignment.setStatusSequence(Status.CREATED.sequence);
            requestedAssignment.setCreated(LocalDateTime.now());
        });
        AssignmentRequest parsedRequest = new AssignmentRequest(new Request(), Collections.emptyList());
        parsedRequest.setRequest(request);
        parsedRequest.setRequestedRoles(requestedAssignments);

        return parsedRequest;
    }

    private void setCorrelationId(Request request) {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes())
            .getRequest();
        request.setCorrelationId(correlationInterceptorUtil.preHandle(httpServletRequest));
    }

    public String getCorrelationId() {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes())
            .getRequest();
        return correlationInterceptorUtil.preHandle(httpServletRequest);
    }

    public void removeCorrelationLog() {
        correlationInterceptorUtil.afterCompletion();
    }

    public Request prepareDeleteRequest(String process, String reference, String actorId, String assignmentId) {
        if (!StringUtils.isEmpty(actorId)) {
            ValidationUtil.validateInputParams(Constants.UUID_PATTERN, actorId);
        }

        Request request = Request.builder()
                                 .clientId(securityUtils.getServiceId())
                                 .authenticatedUserId(UUID.fromString(securityUtils.getUserId()))
                                 .status(Status.CREATED)
                                 .requestType(RequestType.DELETE)
                                 .created(LocalDateTime.now())
                                 .process(process)
                                 .reference(reference)
                                 .build();
        setCorrelationId(request);
        setAssignerId(request);

        if (!StringUtils.isEmpty(assignmentId)) {
            ValidationUtil.validateInputParams(Constants.UUID_PATTERN, assignmentId);
            request.setRoleAssignmentId(UUID.fromString(assignmentId));
        }
        return request;
    }

    private void setAssignerId(Request request) {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes())
            .getRequest();
        String assignerId = httpServletRequest.getHeader("assignerId");

        if (StringUtils.isBlank(assignerId)) {
            request.setAssignerId(request.getAuthenticatedUserId());
        } else {
            ValidationUtil.validateInputParams(Constants.UUID_PATTERN, assignerId);
            request.setAssignerId(UUID.fromString(assignerId));
        }
    }
}
