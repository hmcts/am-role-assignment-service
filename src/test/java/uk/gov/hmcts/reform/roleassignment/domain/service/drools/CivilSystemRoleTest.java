package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.STANDARD;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@ExtendWith(MockitoExtension.class)
class CivilSystemRoleTest extends DroolBase {

    static final String CIVIL_CLIENT_ID = "civil_service";
    static final String CIVIL_PROCESS_ID = "civil-system-user";
    static final String CIVIL_REFERENCE_CBUS = "civil-cbus-system-user";
    static final String CIVIL_REFERENCE_HEARINGS = "civil-hearings-system-user";
    static final String CIVIL_REFERENCE_CASE_ALLOCATOR = "civil-case-allocator-system-user";

    @ParameterizedTest
    @CsvSource({
        "cbus-system-user,SYSTEM,CIVIL,UK"
    })
    void shouldApproveCivilOrgRequestedRoleForCbusSystemUser(String roleName,
                                                             String roleCategory,
                                                             String jurisdiction,
                                                             String primaryLocation) {
        assignmentRequest.getRequest().setClientId(CIVIL_CLIENT_ID);
        assignmentRequest.getRequest().setProcess(CIVIL_PROCESS_ID);
        assignmentRequest.getRequest().setReference(CIVIL_REFERENCE_CBUS);
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("primaryLocation", convertValueJsonNode(primaryLocation));
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        buildExecuteKieSession();

        //assertion
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(APPROVED, roleAssignment.getStatus());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "cbus-system-user,SYSTEM,CIVIL"
    })
    void shouldDeleteCivilOrgRequestedRoleForCbusSystemUser(String roleName,
                                                            String roleCategory,
                                                            String jurisdiction) {
        assignmentRequest.getRequest().setClientId(CIVIL_CLIENT_ID);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        buildExecuteKieSession();

        //assertion
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(DELETE_APPROVED, roleAssignment.getStatus());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "hearing-manager,SYSTEM,CIVIL,CIVIL",
        "hearing-viewer,SYSTEM,CIVIL,CIVIL",
        "hearing-manager,SYSTEM,CIVIL,OTHER_CIVIL_CASE_TYPE",
        "hearing-viewer,SYSTEM,CIVIL,OTHER_CIVIL_CASE_TYPE"
    })
    void shouldApproveCivilOrgRequestedRoleForHearingSystemUser(String roleName,
                                                                String roleCategory,
                                                                String jurisdiction,
                                                                String caseType) {
        assignmentRequest.getRequest().setClientId(CIVIL_CLIENT_ID);
        assignmentRequest.getRequest().setProcess(CIVIL_PROCESS_ID);
        assignmentRequest.getRequest().setReference(CIVIL_REFERENCE_HEARINGS);
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            roleAssignment.getAttributes().put("caseType", convertValueJsonNode(caseType));
        });

        buildExecuteKieSession();

        //assertion
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(APPROVED, roleAssignment.getStatus());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "hearing-manager,SYSTEM,CIVIL",
        "hearing-viewer,SYSTEM,CIVIL"
    })
    void shouldDeleteCivilOrgRequestedRoleForHearingSystemUser(String roleName,
                                                               String roleCategory,
                                                               String jurisdiction) {
        assignmentRequest.getRequest().setClientId(CIVIL_CLIENT_ID);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        buildExecuteKieSession();

        //assertion
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(DELETE_APPROVED, roleAssignment.getStatus());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "hearing-manager,SYSTEM,SSCS,Benefit",
        "hearing-viewer,SYSTEM,SSCS,Benefit"
    })
    void shouldRejectCivilOrgRequestedRoleForHearingFromAnotherJurisdiction(String roleName,
                                                                            String roleCategory,
                                                                            String jurisdiction,
                                                                            String caseType) {
        assignmentRequest.getRequest().setClientId(CIVIL_CLIENT_ID);
        assignmentRequest.getRequest().setProcess(CIVIL_PROCESS_ID);
        assignmentRequest.getRequest().setReference(CIVIL_REFERENCE_HEARINGS);
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
            roleAssignment.getAttributes().put("caseType", convertValueJsonNode(caseType));
        });

        buildExecuteKieSession();

        //assertion
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "case-allocator,SYSTEM,CIVIL"
    })
    void shouldApproveCivilOrgRequestedRoleForCaseAllocatorSystemUser(String roleName,
                                                                      String roleCategory,
                                                                      String jurisdiction) {
        assignmentRequest.getRequest().setClientId(CIVIL_CLIENT_ID);
        assignmentRequest.getRequest().setProcess(CIVIL_PROCESS_ID);
        assignmentRequest.getRequest().setReference(CIVIL_REFERENCE_CASE_ALLOCATOR);
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        buildExecuteKieSession();

        //assertion
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(APPROVED, roleAssignment.getStatus());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "case-allocator,SYSTEM,CIVIL"
    })
    void shouldDeleteCivilOrgRequestedRoleForCaseAllocatorSystemUser(String roleName,
                                                                     String roleCategory,
                                                                     String jurisdiction) {
        assignmentRequest.getRequest().setClientId(CIVIL_CLIENT_ID);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        buildExecuteKieSession();

        //assertion
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(DELETE_APPROVED, roleAssignment.getStatus());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }

    @ParameterizedTest
    @CsvSource({
        "case-allocator,SYSTEM,IA",
        "case-allocator,SYSTEM,PRIVATELAW"
    })
    void shouldRejectCivilOrgRequestedRoleForCaseAllocatorFromAnotherJurisdiction(String roleName,
                                                                                  String roleCategory,
                                                                                  String jurisdiction) {
        assignmentRequest.getRequest().setClientId(CIVIL_CLIENT_ID);
        assignmentRequest.getRequest().setProcess(CIVIL_PROCESS_ID);
        assignmentRequest.getRequest().setReference(CIVIL_REFERENCE_CASE_ALLOCATOR);
        assignmentRequest.getRequest().setReplaceExisting(true);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.valueOf(roleCategory));
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        buildExecuteKieSession();

        //assertion
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(Status.REJECTED, roleAssignment.getStatus());
        });
    }

}
