package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatastoreFeignClientInterceptorTest {

    @Mock
    private IdamRepository idamRepositoryMock;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private RequestTemplate restTemplate;

    private DatastoreFeignClientInterceptor datastoreFeignClientInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        datastoreFeignClientInterceptor = new DatastoreFeignClientInterceptor();
        datastoreFeignClientInterceptor.idamRepository = idamRepositoryMock;
        datastoreFeignClientInterceptor.securityUtils = securityUtils;
    }

    @Test
    void requestInterceptorConsumerWithConditionMet() {
        RequestInterceptor interceptor = datastoreFeignClientInterceptor.requestInterceptor();
        assertNotNull(interceptor);

        when(restTemplate.url()).thenReturn("badok");
        when(restTemplate.header(anyString(), anyString())).thenReturn(restTemplate);
        when(securityUtils.getServiceAuthorizationHeader()).thenReturn("Ok");
        interceptor.apply(restTemplate);
        verify(restTemplate).header(Constants.SERVICE_AUTHORIZATION2,"Bearer Ok");

    }


    @Test
    void requestInterceptorConsumerWithNoConditionMet() {
        RequestInterceptor interceptor = datastoreFeignClientInterceptor.requestInterceptor();
        assertNotNull(interceptor);

        when(restTemplate.url()).thenReturn("healthok");
        when(restTemplate.header(anyString(), anyString())).thenReturn(restTemplate);
        when(securityUtils.getServiceAuthorizationHeader()).thenReturn("Ok");
        interceptor.apply(restTemplate);
        verify(restTemplate, times(0)).header(anyString(), anyString());
    }
}
