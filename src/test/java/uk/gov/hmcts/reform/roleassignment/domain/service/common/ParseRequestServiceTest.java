package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.util.CorrelationInterceptorUtil;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATED;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
class ParseRequestServiceTest {

    @InjectMocks
    private ParseRequestService sut = new ParseRequestService();

    @Mock
    private SecurityUtils securityUtilsMock = mock(SecurityUtils.class);

    @Mock
    private CorrelationInterceptorUtil correlationInterceptorUtilMock = mock(CorrelationInterceptorUtil.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parseRequest_CreateEndpoint_HappyPath() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        String clientId = "copied client id";
        UUID userId = UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c");
        when(securityUtilsMock.getServiceId()).thenReturn(clientId);
        when(securityUtilsMock.getUserId()).thenReturn(userId.toString());
        when(correlationInterceptorUtilMock.preHandle(
            any(HttpServletRequest.class))).thenReturn("21334a2b-79ce-44eb-9168-2d49a744be9d");
        RequestType requestType = RequestType.CREATE;
        AssignmentRequest assignmentRequest =  TestDataBuilder.buildAssignmentRequest(CREATED);
        AssignmentRequest result = sut.parseRequest(assignmentRequest, requestType);

        assertNotNull(result);
        assertNotNull(result.getRequest());
        assertNotNull(result.getRequestedRoles());
        assertEquals(clientId, result.getRequest().getClientId());
        assertEquals(userId, result.getRequest().getAuthenticatedUserId());
        assertEquals(CREATED, result.getRequest().getStatus());
        assertEquals(requestType, result.getRequest().getRequestType());
        assertNotNull(result.getRequest().getCreated());

        RoleAssignment refRoleAssignment = assignmentRequest.getRequestedRoles().iterator().next();
        result.getRequestedRoles().forEach(requestedRole -> {
            assertEquals(refRoleAssignment.getProcess(), requestedRole.getProcess());
            assertEquals(refRoleAssignment.getReference(), requestedRole.getReference());
            assertEquals(refRoleAssignment.getStatus(), requestedRole.getStatus());
            assertEquals(refRoleAssignment.getStatusSequence(), requestedRole.getStatusSequence());
        });
        verify(securityUtilsMock, times(1)).getServiceId();
        verify(securityUtilsMock, times(1)).getUserId();
        verify(correlationInterceptorUtilMock, times(1)).preHandle(any(HttpServletRequest.class));
    }

    @Test
    @DisplayName("should throw 400 exception for a syntactically bad Assignment id")
    void shouldThrowBadRequestForMalformedAssignmentId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String assignmentId = "you_cant_see_this_malformed_id";
        UUID userId = UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c");
        when(securityUtilsMock.getUserId()).thenReturn(userId.toString());
        Assertions.assertThrows(BadRequestException.class, () -> {
            sut.prepareDeleteRequest(null, null, null, assignmentId);
        });
    }

    @Test
    void getCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        when(correlationInterceptorUtilMock.preHandle(
            any(HttpServletRequest.class))).thenReturn("21334a2b-79ce-44eb-9168-2d49a744be9d");
        assertEquals("21334a2b-79ce-44eb-9168-2d49a744be9d", sut.getCorrelationId());
        verify(correlationInterceptorUtilMock, times(1)).preHandle(any(HttpServletRequest.class));
    }

    @Test
    void prepareDeleteRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        String clientId = "copied client id";
        UUID userId = UUID.fromString("21334a2b-79ce-44eb-9168-2d49a744be9c");
        when(securityUtilsMock.getServiceId()).thenReturn(clientId);
        when(securityUtilsMock.getUserId()).thenReturn(userId.toString());
        when(correlationInterceptorUtilMock.preHandle(
            any(HttpServletRequest.class))).thenReturn("21334a2b-79ce-44eb-9168-2d49a744be9d");

        Request builtReq = TestDataBuilder.buildRequest(CREATED);
        Request result = sut.prepareDeleteRequest(builtReq.getProcess(),builtReq.getReference(),
                                                  "21334a2b-79ce-44eb-9168-2d49a744be9d",
                                                  "21334a2b-79ce-44eb-9168-2d49a744be9d");
        builtReq.setRequestType(RequestType.DELETE);

        assertEquals(clientId, result.getClientId());
        assertEquals(userId.toString(), result.getAuthenticatedUserId().toString());
        assertEquals(builtReq.getStatus(), result.getStatus());
        assertEquals(builtReq.getRequestType(), result.getRequestType());
        assertEquals(builtReq.getProcess(), result.getProcess());
        assertEquals(builtReq.getReference(), result.getReference());
        assertEquals("21334a2b-79ce-44eb-9168-2d49a744be9c", result.getAssignerId().toString());
        assertEquals("21334a2b-79ce-44eb-9168-2d49a744be9d", result.getCorrelationId());
    }
}
