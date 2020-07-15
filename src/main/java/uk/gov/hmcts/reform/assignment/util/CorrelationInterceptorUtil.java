package uk.gov.hmcts.reform.assignment.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.assignment.apihelper.Constants;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

@Component
public class CorrelationInterceptorUtil  {
    private static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

    public String preHandle(final HttpServletRequest request) throws Exception {
        final String correlationId = getCorrelationIdFromHeader(request);
        MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
        return correlationId;
    }

    public void afterCompletion() {
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }

    private String getCorrelationIdFromHeader(final HttpServletRequest request) {
        String correlationId = request.getHeader(Constants.CORRELATION_ID_HEADER_NAME);
        if (StringUtils.isBlank(correlationId)) {
            correlationId = generateUniqueCorrelationId();
        }
        return correlationId;
    }

    private String generateUniqueCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
