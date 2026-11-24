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
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_APPROVED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.getRequestedOrgRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

@ExtendWith(MockitoExtension.class)
class PossessionsSystemRoleTest extends DroolBase {

    static final String PCS_CLIENT_ID = "pcs_api";
    static final String PCS_JURISDICTION = "PCS";
    static final String PCS_PROCESS_ID = "pcs-system-user";
    static final String PCS_REFERENCE_HEARINGS = "pcs-hearings-system-user";


    @ParameterizedTest
    @CsvSource({
        "hearing-manager," + PCS_CLIENT_ID + "," + PCS_PROCESS_ID + "," + PCS_REFERENCE_HEARINGS,
        "hearing-viewer," + PCS_CLIENT_ID + "," + PCS_PROCESS_ID + "," + PCS_REFERENCE_HEARINGS
    })
    void shouldApproveOrRejectPcsSystemOrgRoleRequest(String roleName,
                                                      String clientId,
                                                      String process,
                                                      String reference) {

        RoleCategory roleCategory = RoleCategory.SYSTEM;

        // wrong category
        verifySystemOrgRoleRequest(RoleCategory.CITIZEN, // WRONG
                                   roleName,
                                   clientId,
                                   process,
                                   reference,
                                   PCS_JURISDICTION,
                                   true,
                                   Status.REJECTED);
        // wrong roleName
        verifySystemOrgRoleRequest(roleCategory,
                                   "wrong-roleName", // WRONG
                                   clientId,
                                   process,
                                   reference,
                                   PCS_JURISDICTION,
                                   true,
                                   Status.REJECTED);
        // wrong clientId
        verifySystemOrgRoleRequest(roleCategory,
                                   roleName,
                                   "wrong-clientId", // WRONG
                                   process,
                                   reference,
                                   PCS_JURISDICTION,
                                   true,
                                   Status.REJECTED);
        // wrong process
        verifySystemOrgRoleRequest(roleCategory,
                                   roleName,
                                   clientId,
                                   "wrong-process", // WRONG
                                   reference,
                                   PCS_JURISDICTION,
                                   true,
                                   Status.REJECTED);
        // wrong reference
        verifySystemOrgRoleRequest(roleCategory,
                                   roleName,
                                   clientId,
                                   process,
                                   "wrong-reference", // WRONG
                                   PCS_JURISDICTION,
                                   true,
                                   Status.REJECTED);
        // wrong jurisdiction
        verifySystemOrgRoleRequest(roleCategory,
                                   roleName,
                                   clientId,
                                   process,
                                   reference,
                                   "wrong-jurisdiction", // WRONG
                                   true,
                                   Status.REJECTED);
        // wrong replace existing
        verifySystemOrgRoleRequest(roleCategory,
                                   roleName,
                                   clientId,
                                   process,
                                   reference,
                                   PCS_JURISDICTION,
                                   false, // WRONG
                                   Status.REJECTED);

        // correct values should be approved
        verifySystemOrgRoleRequest(roleCategory,
                                   roleName,
                                   clientId,
                                   process,
                                   reference,
                                   PCS_JURISDICTION,
                                   true,
                                   Status.APPROVED);
    }

    private void verifySystemOrgRoleRequest(RoleCategory roleCategory,
                                            String roleName,
                                            String clientId,
                                            String process,
                                            String reference,
                                            String jurisdiction,
                                            boolean replaceExisting,
                                            Status expectedStatus) {

        // GIVEN
        assignmentRequest.getRequest().setClientId(clientId);
        assignmentRequest.getRequest().setProcess(process);
        assignmentRequest.getRequest().setReference(reference);
        assignmentRequest.getRequest().setReplaceExisting(replaceExisting);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(roleCategory);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(CREATE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(jurisdiction));
        });

        // WHEN
        buildExecuteKieSession();

        // THEN
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(expectedStatus, roleAssignment.getStatus());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }


    @ParameterizedTest
    @CsvSource({
        "hearing-manager," + PCS_CLIENT_ID + "," + PCS_PROCESS_ID + "," + PCS_REFERENCE_HEARINGS,
        "hearing-viewer," + PCS_CLIENT_ID + "," + PCS_PROCESS_ID + "," + PCS_REFERENCE_HEARINGS
    })
    void shouldDeletePcsSystemOrgRole(String roleName,
                                      String clientId,
                                      String process,
                                      String reference) {

        // GIVEN
        assignmentRequest.getRequest().setClientId(clientId);
        assignmentRequest.getRequest().setProcess(process);
        assignmentRequest.getRequest().setReference(reference);
        assignmentRequest.setRequestedRoles(getRequestedOrgRole());
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            roleAssignment.setRoleCategory(RoleCategory.SYSTEM);
            roleAssignment.setRoleType(RoleType.ORGANISATION);
            roleAssignment.setRoleName(roleName);
            roleAssignment.setGrantType(STANDARD);
            roleAssignment.setStatus(Status.DELETE_REQUESTED);
            roleAssignment.getAttributes().put("jurisdiction", convertValueJsonNode(PCS_JURISDICTION));
        });

        // WHEN
        buildExecuteKieSession();

        // THEN
        assertTrue(CollectionUtils.isNotEmpty(assignmentRequest.getRequestedRoles()));
        assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
            assertEquals(DELETE_APPROVED, roleAssignment.getStatus());
            assertEquals(roleName, roleAssignment.getRoleName());
            assertEquals(PCS_JURISDICTION, roleAssignment.getAttributes().get("jurisdiction").asText());
        });
    }

}
