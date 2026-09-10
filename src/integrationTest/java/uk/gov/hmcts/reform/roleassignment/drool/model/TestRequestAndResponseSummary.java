package uk.gov.hmcts.reform.roleassignment.drool.model;

import lombok.Getter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

@Getter
public class TestRequestAndResponseSummary {

    public TestRequestAndResponseSummary(MvcResult result) {
        MockHttpServletRequest request = result.getRequest();
        this.requestMethod = request.getMethod();
        this.requestUri = request.getRequestURI();

        this.requestHeaders = new HashMap<>();
        var requestHeaderNames = request.getHeaderNames();
        while (requestHeaderNames.hasMoreElements()) {
            String header = requestHeaderNames.nextElement();
            this.requestHeaders.put(header, request.getHeader(header));
        }

        MockHttpServletResponse response = result.getResponse();
        this.responseStatus = response.getStatus();

        this.responseHeaders = new HashMap<>();
        response.getHeaderNames().forEach((headerName -> {
            responseHeaders.put(headerName, response.getHeader(headerName));
        }));
    }

    String requestMethod;
    String requestUri;
    Map<String, String> requestHeaders;

    int responseStatus;
    Map<String, String> responseHeaders;

}
