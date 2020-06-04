package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.apihelper.Constants;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

@Service
public class DatastoreFeignClientInterceptor {

    @Autowired SecurityUtils securityUtils;
    @Bean
    public RequestInterceptor requestInterceptor() {
        //HttpHeaders headers = securityUtils.authorizationHeaders();
        return requestTemplate -> {
            requestTemplate.header(Constants.SERVICE_AUTHORIZATION2, "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbV9yb2xlX2Fzc2lnbm1lbnRfc2VydmljZSIsImV4cCI6MTU5MTI2NjYzNn0.fm-BwXCIRpBZx_e3H0MNMduA_Yv4NXv9UQzsv2P9ymydwfkeAcuFeMzwl75G3EDImLs9xYi8KuNOlsIOmTQn7A");
            requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhdXRvLnRlc3QuY25wQGdtYWlsLmNvbSIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjkxOGI1YjgyLTMyMTUtNDJmNy04YTA3LTNiYjRkYjE1NTM4NyIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI5MTgxZDViOC1kOWFjLTQyYWMtYjQyMy1jOGNhOTc2ZmVjMzUiLCJhdWQiOiJjY2RfZ2F0ZXdheSIsIm5iZiI6MTU5MTI1MjE4NSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE1OTEyNTIxODUwMDAsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNTkxMjgwOTg1LCJpYXQiOjE1OTEyNTIxODUsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiI2MDNlY2ZjNS1hMDMzLTQ1YzEtODg3Yi1iY2U3Njg3NGQxODkifQ.l2wMh6AHnfhoEIc1_cwo8BC4dnFEBiCjgpTCEpzKavrZ6fMd_M9IW-Lr4W1pTg_s47XxqPGt9IaMENC9V6jp_El6wsQKtA2hys5hLkGJPPZ8KixDNRCB8f56_v4AmYQnqSlYOy8-bRni3MtQ9yg18F-qm97kBQksmLjInjdKb0jVPEq827304ZT8c49gx10YSCshsfwUstsWVpSL4CV6YEpL-OErFvbTEhI1a1-4g0YoMJkCO9vhcdt4iWPhLvBWw65-fPXMcf4m0A6zkEhUsriCYkYMr59N16cIjZuhpCFNcD_Ig1zV4HqAogV5wki2jemhjCe1Gr8EprTFbJUu-w");
            requestTemplate.header(HttpHeaders.CONTENT_TYPE, "application/json");
            requestTemplate.header("experimental", "true");
        };
    }
}
