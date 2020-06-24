package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.CorrelationInterceptorUtil;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(correlationInterceptorUtilMock.preHandle(any(HttpServletRequest.class))).thenReturn("21334a2b-79ce-44eb-9168-2d49a744be9d");
        RequestType requestType = RequestType.CREATE;
        AssignmentRequest assignmentRequest =  TestDataBuilder.buildAssignmentRequest();
        AssignmentRequest result = sut.parseRequest(assignmentRequest, requestType);

        assertNotNull(result);
        assertNotNull(result.getRequest());
        assertNotNull(result.getRequestedRoles());
        assertEquals(clientId, result.getRequest().getClientId());
        assertEquals(userId, result.getRequest().getAuthenticatedUserId());
        assertNotNull(result.getRequest().getStatus());
        assertEquals(requestType, result.getRequest().getRequestType());
        assertNotNull(result.getRequest().getCreated());
        result.getRequestedRoles().forEach(requestedRole -> {
            assertNotNull(requestedRole.getProcess());
            assertNotNull(requestedRole.getReference());
            assertNotNull(requestedRole.getStatus());
            assertNotNull(requestedRole.getStatusSequence());
        });
        verify(securityUtilsMock, times(1)).getServiceId();
        verify(securityUtilsMock, times(1)).getUserId();
        verify(correlationInterceptorUtilMock, times(1)).preHandle(any(HttpServletRequest.class));
    }

}
