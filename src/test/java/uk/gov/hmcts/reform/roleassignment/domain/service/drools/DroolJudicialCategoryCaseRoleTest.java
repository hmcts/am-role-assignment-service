package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.CHALLENGED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedCaseRole;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedCaseRole_ra;

@RunWith(MockitoJUnitRunner.class)
class DroolJudicialCategoryCaseRoleTest extends DroolBase {

    HashMap<String, JsonNode> attributes;

    public DroolJudicialCategoryCaseRoleTest() {
        attributes = new HashMap<>();
        attributes.put("jurisdiction", convertValueJsonNode("IA"));
        attributes.put("caseType", convertValueJsonNode("Asylum"));
    }

    @Test
    void shouldApproveRequestedRoleForCase_CaseAllocator() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.APPROVED, roleAssignment.getStatus());
            assertEquals("N", roleAssignment.getAttributes().get("substantive").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "case-allocator",
        "lead-judge",
        "hearing-judge",
        "ftpa-judge",
        "hearing-panel-judge"
    })
    void shouldDeleteApprovedRequestedRoleForCase_CaseAllocator(String roleName) {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                roleName,
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        FeatureFlag featureFlag1  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_1_1.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);
        featureFlags.add(featureFlag1);

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        //existingAttributes.put("caseType", convertValueJsonNode("Asylum"));

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              roleName,
                                              RoleCategory.JUDICIAL, existingAttributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, existingAttributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_APPROVED, ra.getStatus()));
    }

    @Test
    void shouldApproveRequestedRoleForCase_JudgeRoles_Lead_Hearing() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "lead-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              "leadership-judge",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));
    }

    @Test
    void shouldApproveRequestedRoleForCase_JudgeRoles_Ftpa_HearingPanel() {


        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "ftpa-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);
        RoleAssignment requestedRole2 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "hearing-panel-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1, requestedRole2));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              "judge",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(requestedRole2.getActorId(),
                                              "judge",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.APPROVED, ra.getStatus()));
    }

    @Test
    void shouldRejectRequestedRole_WrongRequesterRoleName() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "hearing-panel-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              "hearing-panel-judge",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                              "Hearing-panel-judge",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    void shouldRejectRequestedRoleForWrongData_GrantType() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                CHALLENGED, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        Map<String, JsonNode> attributes = new HashMap<>();
        attributes.put("jurisdiction", convertValueJsonNode("IA"));
        attributes.put("caseType", convertValueJsonNode("Asylum"));

        executeDroolRules(List.of(buildExistingRole(requestedRole1.getActorId(),
                                                    "case-allocator",
                                                    RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                                    Classification.PUBLIC, GrantType.STANDARD,Status.LIVE
                                  ),
                                  buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                                          "case-allocator",
                                                          RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                                    Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }

    @Test
    void shouldRejectRequestedRole_WrongAssignerID() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId() + "12",
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectRequestedRoleForDelete_WrongAssignerID() {

        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", DELETE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId() + "12",
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles()
            .forEach(roleAssignment -> assertEquals(Status.DELETE_REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectRequestedRole_MissingCaseId() {
        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "case-allocator",
                                                             SPECIFIC);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectCaseValidation_MissingExistingRA() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL, "lead-judge",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);
        assignmentRequest.setRequestedRoles(List.of(requestedRole1));

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);


        executeDroolRules(List.of(TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                                                    "case-allocator",
                                                                    RoleCategory.JUDICIAL,
                                                                    attributes, RoleType.ORGANISATION,
                                                                    Classification.PUBLIC, GrantType.STANDARD,
                                                                    Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectRequestedRoleF_WrongRequesterJurisdiction() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "case-allocator",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        ExistingRoleAssignment existingActorAssignment1 = TestDataBuilder.buildExistingRole(
            requestedRole1.getActorId(),
            "case-allocator",
            RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
            Classification.PUBLIC, GrantType.STANDARD,Status.LIVE
        );

        ExistingRoleAssignment existingRequesterAssignment1 = TestDataBuilder.buildExistingRole(
            assignmentRequest.getRequest().getAssignerId(), "case-allocator",
            RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
            Classification.PUBLIC, GrantType.STANDARD,Status.LIVE);

        existingRequesterAssignment1.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain the request
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectRequestedRoleF_WrongAssigneeJurisdiction() {

        RoleAssignment requestedRole1 = getRequestedCaseRole(RoleCategory.JUDICIAL, "lead-judge",
                                                             SPECIFIC);
        requestedRole1.getAttributes().put("caseId", convertValueJsonNode("1234567890123456"));

        List<RoleAssignment> requestedRoles = new ArrayList<>();
        requestedRoles.add(requestedRole1);
        assignmentRequest.setRequestedRoles(requestedRoles);

        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(true).build();
        featureFlags.add(featureFlag);

        ExistingRoleAssignment existingActorAssignment1 = TestDataBuilder.buildExistingRole(
            requestedRole1.getActorId(),
            "leadership-judge",
            RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
            Classification.PUBLIC, GrantType.STANDARD,Status.LIVE
        );

        ExistingRoleAssignment existingRequesterAssignment1 = TestDataBuilder.buildExistingRole(
            assignmentRequest.getRequest().getAssignerId(), "case-allocator",
            RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
            Classification.PUBLIC, GrantType.STANDARD,Status.LIVE);

        existingActorAssignment1.getAttributes().put("jurisdiction", convertValueJsonNode("CMC"));

        List<ExistingRoleAssignment> existingRoleAssignments = new ArrayList<>();
        existingRoleAssignments.add(existingActorAssignment1);
        existingRoleAssignments.add(existingRequesterAssignment1);

        // facts must contain the request
        executeDroolRules(existingRoleAssignments);

        //assertion
        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @Test
    void shouldRejectRequestedRoleForCreate_IACFlagFalse() {
        RoleAssignment requestedRole1 = getRequestedCaseRole_ra(RoleCategory.JUDICIAL,
                                                                "case-allocator",
                                                                SPECIFIC, "caseId",
                                                                "1234567890123456", CREATE_REQUESTED);

        assignmentRequest.setRequestedRoles(List.of(requestedRole1));
        FeatureFlag featureFlag  =  FeatureFlag.builder().flagName(FeatureFlagEnum.IAC_JRD_1_0.getValue())
            .status(false).build();
        featureFlags.add(featureFlag);

        executeDroolRules(List.of(
            TestDataBuilder.buildExistingRole(requestedRole1.getActorId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE),
            TestDataBuilder.buildExistingRole(assignmentRequest.getRequest().getAssignerId(),
                                              "case-allocator",
                                              RoleCategory.JUDICIAL, attributes, RoleType.ORGANISATION,
                                              Classification.PUBLIC, GrantType.STANDARD,Status.LIVE)));

        //assertion
        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
    }


}
