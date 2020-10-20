package uk.gov.hmcts.reform.roleassignment.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        response.addHeader(Constants.CORRELATION_ID_HEADER_NAME, correlationInterceptorUtil.preHandle(request));
        filterChain.doFilter(request, response);
    }
}
