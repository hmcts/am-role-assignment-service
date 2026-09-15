package uk.gov.hmcts.reform.roleassignment.security;

import io.restassured.specification.RequestSpecification;
import uk.gov.hmcts.reform.roleassignment.controller.BaseAuthorisedTestIntegration;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.util.Collections;

import static uk.gov.hmcts.reform.roleassignment.controller.utils.TestAuthenticationUtils.getJwtHeaders;
import static uk.gov.hmcts.reform.roleassignment.controller.utils.WireMockStubs.SERVICE_NAME;

public class BaseSecurityIntegrationTest extends BaseAuthorisedTestIntegration {

    protected static final String VALID_ISSUER_1 = "http://localhost:5062/o";
    protected static final String VALID_ISSUER_2 = "https://secondary-idam.platform.hmcts.net";
    protected static final String ROGUE_ISSUER = "https://rogue-issuer.com";

    protected static final AssignmentRequest ASSIGNMENT_REQUEST =
        AssignmentRequest.builder()
            .request(Request.builder()
                         .assignerId(ACTOR_ID1)
                         .process("process")
                         .reference("reference")
                         .status(Status.CREATED)
                         .replaceExisting(true)
                         .build())
            .requestedRoles(Collections.emptyList())//TestDataBuilder.buildRequestedRoles(true))
            .build();

    protected RequestSpecification jwtRequest(
            String issuer,
            boolean expired)
            throws Exception {

        return getRequestSpecification(
            SERVICE_NAME,
                ACTOR_ID1,
                getJwtHeaders(issuer, expired));
    }

    protected RequestSpecification unexpiredJwt(
            String issuer)
            throws Exception {

        return jwtRequest(issuer, false);
    }

    protected RequestSpecification expiredJwt(
            String issuer)
            throws Exception {

        return jwtRequest(issuer, true);
    }
}
