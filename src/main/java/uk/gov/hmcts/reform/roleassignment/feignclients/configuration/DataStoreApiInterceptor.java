package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;
import uk.gov.hmcts.reform.roleassignment.oidc.OIdcAdminConfiguration;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

@Service
public class DataStoreApiInterceptor {

    SecurityUtils securityUtils;
    IdamRepository idamRepository;
    OIdcAdminConfiguration oidcAdminConfiguration;

    public DataStoreApiInterceptor(SecurityUtils securityUtils,IdamRepository idamRepository,
                                   OIdcAdminConfiguration oidcAdminConfiguration) {
        this.securityUtils = securityUtils;
        this.idamRepository = idamRepository;
        this.oidcAdminConfiguration = oidcAdminConfiguration;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (!requestTemplate.url().contains("health")) {
                requestTemplate.header(Constants.SERVICE_AUTHORIZATION2, "Bearer "
                    + securityUtils.getServiceAuthorizationHeader());
                requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer "
                    + idamRepository.getManageUserToken(oidcAdminConfiguration.getUserId()));
                requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
            }
        };
    }
}

