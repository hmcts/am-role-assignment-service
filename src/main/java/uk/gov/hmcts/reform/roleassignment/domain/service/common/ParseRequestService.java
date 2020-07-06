package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.CorrelationInterceptorUtil;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.UUID_PATTERN;

@Service
public class ParseRequestService {

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private CorrelationInterceptorUtil correlationInterceptorUtil;

    private String serviceId;
    private String userId;

    public AssignmentRequest parseRequest(AssignmentRequest assignmentRequest, RequestType requestType)
        throws Exception {
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
        AssignmentRequest parsedRequest = new AssignmentRequest();
        parsedRequest.setRequest(request);
        parsedRequest.setRequestedRoles(requestedAssignments);

        return parsedRequest;
    }

    private void setCorrelationId(Request request) throws Exception {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes())
            .getRequest();
        request.setCorrelationId(correlationInterceptorUtil.preHandle(httpServletRequest));
    }

    public String getCorrelationId() throws Exception {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes())
            .getRequest();
        return correlationInterceptorUtil.preHandle(httpServletRequest);
    }

    public void removeCorrelationLog() throws Exception {
        correlationInterceptorUtil.afterCompletion();
    }

    public Request prepareDeleteRequest(String process, String reference, String actorId) throws Exception {
        if (actorId != null) {
            ValidationUtil.validateInputParams(UUID_PATTERN, actorId);
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
            request.setAssignerId(UUID.fromString(assignerId));
        }
        ValidationUtil.validateInputParams(UUID_PATTERN, request.getAssignerId().toString());

    }
}
