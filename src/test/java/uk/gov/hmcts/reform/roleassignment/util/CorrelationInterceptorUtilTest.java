package uk.gov.hmcts.reform.roleassignment.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class CorrelationInterceptorUtilTest {

    @InjectMocks
    private CorrelationInterceptorUtil sut = new CorrelationInterceptorUtil();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void preHandle() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(Constants.CORRELATION_ID_HEADER_NAME, "6b36bfc6-bb21-11ea-b3de-0242ac132003");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String result = sut.preHandle(request);
        assertNotNull(result);
        assertEquals("6b36bfc6-bb21-11ea-b3de-0242ac132003", result);
    }

    @Test
    void preHandle_blank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(Constants.CORRELATION_ID_HEADER_NAME, "");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String result = sut.preHandle(request);
        UUID validUuid = UUID.fromString(result);
        assertNotNull(validUuid);
        sut.afterCompletion();
    }
}
