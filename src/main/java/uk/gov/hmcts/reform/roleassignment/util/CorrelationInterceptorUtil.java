package uk.gov.hmcts.reform.roleassignment.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Component
public class CorrelationInterceptorUtil  {

    public String preHandle(final HttpServletRequest request) {
        ValidationUtil.validateId(Constants.NUMBER_TEXT_HYPHEN_PATTERN, getCorrelationIdFromHeader(request));
        String correlationId = getCorrelationIdFromHeader(request);
        MDC.put(Constants.CORRELATION_ID_HEADER_NAME, correlationId);
        return getCorrelationIdFromHeader(request);
    }

    public String getContextCorrelationId() {
        String correlationId = MDC.get(Constants.CORRELATION_ID_HEADER_NAME);
        if (StringUtils.isBlank(correlationId)) {
            correlationId = generateUniqueCorrelationId();
        }
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
            correlationId = ValidationUtil.sanitiseCorrelationId(
                request.getHeader(Constants.CORRELATION_ID_HEADER_NAME));
        }
        return correlationId;
    }

    private String generateUniqueCorrelationId() {
        return UUID.randomUUID().toString();
    }


}
