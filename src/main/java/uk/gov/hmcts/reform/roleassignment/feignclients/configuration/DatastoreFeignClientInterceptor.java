package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import feign.RequestInterceptor;
import org.apache.http.entity.ContentType;
import org.springframework.context.annotation.Bean;

@SuppressWarnings("checkstyle:OperatorWrap")
public class DatastoreFeignClientInterceptor {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header(
                "ServiceAuthorization",
                "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTExOTUzMjN9" +
                ".MGQ8PC6Cs-IlwdaR4DkJrW_odURkS9cFDg_y07wF5CimXFUZ-FmD2vlMAKp1EWJA8LfnIzxCRLHMUDHAI85SOA");
            requestTemplate.header(
                "Authorization",
                "Bearer " +
                "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2" +
                "In0.eyJzdWIiOiJhdXRvLnRlc3QuY25wQGdtYWlsLmNvbSIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjY3YzhjMW" +
                "RjLWE1OGItNDU4Mi04MGY3LWFiNWIwYmY3YWU5MCIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5hbS9vYXV0aDIvaG1jdHMi" +
                "LCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiIxZTYyNDBmYi0xMz" +
                "djLTQxYWQtOTk3ZC1mYjNhZDJjOGMzMjQiLCJhdWQiOiJjY2RfZ2F0ZXdheSIsIm5iZiI6MTU5MTE3ODk4NiwiZ3JhbnRfdHlwZSI" +
                "6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE1OTE" +
                "xNzg5ODYwMDAsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNTkxMjA3Nzg2LCJpYXQiOjE1OTExNzg5ODYsImV4cGlyZXNfaW4iOjI" +
                "4ODAwLCJqdGkiOiIxM2M2NDgzMS1jOWFlLTRlNzktOTc5NS0yZjhkODYyMDhiZjIifQ.H2Fpu3eMxO0Yc_JHwehFJo-lrcM3X_6n" +
                "obmonFO6rGMMUKNc8qBBrsuer5-bOtU5yME9F9-GFauzxyOB9r_hhb7P6-aTDlw_ECXvNKPsKJMOYxVVI2Q6VE02Z7QonKXpARG9KN" +
                "5RyQvvWCpwvMiTTpUvVylqvg8t3TuxRVz8Pfo3d7xcNy_7NRshDD-oOM_dGe-OPkEqZyAByGzKgAe1Yy7xkw_Gtj5WpI5b-JTU82Rm" +
                "1ZKCCrK33ZOiOBlN_QPR1GxBNf9Vw6Y3ndeD6uR6xdLg65E_ddSzozL07hZWfjH01vF1QqQOXgfT5wtNS7oY7vpWmVbMXj5z3CVT7GPf8A");
            requestTemplate.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        };
    }
}
