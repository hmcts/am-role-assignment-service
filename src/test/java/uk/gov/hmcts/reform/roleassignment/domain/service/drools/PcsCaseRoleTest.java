package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PcsCaseRoleTest extends DroolBase {

    private static final String CASE_ID = "b3a0f8e8-7ae8-4b82-b36c-4df8a91e9a11";
    private static final String CASE_TYPE = "PCS_CASE";
    private static final String RBID = "a2f35c0d-2f4c-4b9a-8d1b-6bb138f7b111";
    private static final String CALLER_USER_ID = "f6484bbb-c10e-319b-ab8d-14361a7d2a23";

    @ParameterizedTest
    @CsvSource({
        "pcs-solicitor,PROFESSIONAL,true",
        "pcs-citizen,CITIZEN,true",
        "pcs-ha-user,PROFESSIONAL,true"
    })
    void shouldApproveCreateRequestsWhenFlagEnabled(String roleName, String category, boolean rbidRequired) {
        assignmentRequest = buildAssignmentRequest(roleName,
                                                   RoleCategory.valueOf(category),
                                                   Status.CREATE_REQUESTED,
                                                   RequestType.CREATE,
                                                   rbidRequired);
        enablePcsFlag();

        executeDroolRules(Collections.emptyList());

        assignmentRequest.getRequestedRoles().forEach(role ->
            assertThat(role.getStatus()).isEqualTo(Status.APPROVED)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "pcs-solicitor,PROFESSIONAL,true",
        "pcs-citizen,CITIZEN,true",
        "pcs-ha-user,PROFESSIONAL,true"
    })
    void shouldApproveDeleteRequestsWhenFlagEnabled(String roleName, String category, boolean rbidRequired) {
        assignmentRequest = buildAssignmentRequest(roleName,
                                                   RoleCategory.valueOf(category),
                                                   Status.DELETE_REQUESTED,
                                                   RequestType.DELETE,
                                                   rbidRequired);
        enablePcsFlag();

        executeDroolRules(Collections.emptyList());

        assignmentRequest.getRequestedRoles().forEach(role ->
            assertThat(role.getStatus()).isEqualTo(Status.DELETE_APPROVED)
        );
    }

    @Test
    void shouldLeaveStatusUnchangedWhenFlagDisabled() {
        featureFlags.clear();
        assignmentRequest = buildAssignmentRequest("pcs-solicitor",
                                                   RoleCategory.PROFESSIONAL,
                                                   Status.CREATE_REQUESTED,
                                                   RequestType.CREATE,
                                                   true);

        executeDroolRules(Collections.emptyList());

        assignmentRequest.getRequestedRoles().forEach(role ->
            assertThat(role.getStatus()).isEqualTo(Status.REJECTED)
        );
    }

    private AssignmentRequest buildAssignmentRequest(String roleName,
                                                     RoleCategory roleCategory,
                                                     Status status,
                                                     RequestType requestType,
                                                     boolean includeRbid) {

        Map<String, JsonNode> attributes = new HashMap<>();
        attributes.put("jurisdiction", JacksonUtils.convertValueJsonNode("PCS"));
        attributes.put("caseType", JacksonUtils.convertValueJsonNode(CASE_TYPE));
        attributes.put("caseId", JacksonUtils.convertValueJsonNode(CASE_ID));
        if (includeRbid) {
            attributes.put("rbid", JacksonUtils.convertValueJsonNode(RBID));
        }

        RoleAssignment roleAssignment = RoleAssignment.builder()
            .id(UUID.randomUUID())
            .actorId("target-actor")
            .actorIdType(ActorIdType.IDAM)
            .roleType(RoleType.CASE)
            .roleName(roleName)
            .roleCategory(roleCategory)
            .classification(Classification.PUBLIC)
            .grantType(GrantType.SPECIFIC)
            .status(status)
            .readOnly(false)
            .attributes(attributes)
            .build();

        Request request = Request.builder()
            .id(UUID.randomUUID())
            .assignerId(CALLER_USER_ID)
            .authenticatedUserId(CALLER_USER_ID)
            .clientId("pcs_api")
            .process("PCS")
            .reference("pcs-reference")
            .requestType(requestType)
            .replaceExisting(false)
            .created(ZonedDateTime.now())
            .build();

        return AssignmentRequest.builder()
            .request(request)
            .requestedRoles(List.of(roleAssignment))
            .build();
    }

    private void enablePcsFlag() {
        featureFlags.add(FeatureFlag.builder()
                             .flagName(FeatureFlagEnum.PCS_CASE_ROLES_1_0.getValue())
                             .status(true)
                             .build());
    }
}
