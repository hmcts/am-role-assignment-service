package uk.gov.hmcts.reform.roleassignment.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.domain.model.MutableHttpServletRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

@SuppressWarnings("squid:S5167")
@Component
public class FilterRequestUtil extends OncePerRequestFilter {

    @Autowired
    CorrelationInterceptorUtil correlationInterceptorUtil;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        ValidationUtil.sanitiseCorrelationId(request.getHeader(Constants.CORRELATION_ID_HEADER_NAME));
        final String correlationId = correlationInterceptorUtil.preHandle(request);

        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);

        //adding the id to the request header so subsequent calls do not generate new unique id's

        String whiteList = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-"
            + "[089ab][0-9a-f]{3}-[0-9a-f]{12}$";
        boolean match = Pattern.matches(whiteList, correlationId);
        if (!match) {
            throw new BadRequestException(
                String.format(
                    "The input parameter: \"%s\", does not comply with the required pattern",
                    correlationId
                ));

        } else {
            mutableRequest.putHeader(Constants.CORRELATION_ID_HEADER_NAME, correlationId);
            response.addHeader(Constants.CORRELATION_ID_HEADER_NAME, correlationId);
        }


        filterChain.doFilter(mutableRequest, response);
    }
}
