package uk.gov.hmcts.reform.roleassignment.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.roleassignment.domain.model.MutableHttpServletRequest;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class FilterRequestUtil extends OncePerRequestFilter {

    @Autowired
    CorrelationInterceptorUtil correlationInterceptorUtil;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        final String correlationId =
            ValidationUtil.sanitiseCorrelationId(correlationInterceptorUtil.preHandle(request));
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);

        //adding the id to the request header so subsequent calls do not generate new unique id's
        mutableRequest.putHeader(Constants.CORRELATION_ID_HEADER_NAME, correlationId);
        response.addHeader(Constants.CORRELATION_ID_HEADER_NAME, correlationId);

        filterChain.doFilter(mutableRequest, response);
    }
}
