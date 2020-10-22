package uk.gov.hmcts.reform.roleassignment.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@java.lang.SuppressWarnings("squid:S5167")
@Component
public class CorrelationInterceptorUtil  {

    public String preHandle(final HttpServletRequest request) {
        String correlationId = getCorrelationIdFromHeader(request);
        MDC.put(Constants.CORRELATION_ID_HEADER_NAME, correlationId);
        return correlationId;
    }

    public void afterCompletion() {
        MDC.remove(Constants.CORRELATION_ID_HEADER_NAME);
    }

    private String getCorrelationIdFromHeader(final HttpServletRequest request) {
        String correlationId = "";
        if (StringUtils.isBlank(request.getHeader(Constants.CORRELATION_ID_HEADER_NAME))) {
            correlationId = generateUniqueCorrelationId();
        } else {
            correlationId = request.getHeader(Constants.CORRELATION_ID_HEADER_NAME);
            ValidationUtil.sanitiseCorrelationId(correlationId);
        }
        return correlationId;
    }

    private String generateUniqueCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
