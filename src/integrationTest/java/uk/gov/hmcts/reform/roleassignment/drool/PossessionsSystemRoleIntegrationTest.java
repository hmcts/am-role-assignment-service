package uk.gov.hmcts.reform.roleassignment.drool;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
class PossessionsSystemRoleIntegrationTest extends BaseDroolIntegrationTest {

    static final String PCS_CLIENT_ID = "pcs_api";
    static final String PCS_PROCESS_ID = "pcs-system-user";
    static final String PCS_REFERENCE_HEARINGS = "pcs-hearings-system-user";

    static final String JURISDICTION = "PCS";
    static final String JURISDICTION_WRONG = "IA"; // NB: IA have a number of their own System User role assignments


    @Nested
    @DisplayName("Hearing system roles")
    class HearingSystemRoleTests {

        @Test
        void shouldRegisterPcsSystemUserRole_Hearing_SingleUser() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();

            // WHEN / THEN
            registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUser);
        }

        @Test
        void shouldRegisterPcsSystemUserRole_Hearing_MultipleUser() throws Exception {

            // GIVEN
            var uidSystemUser1 = UUID.randomUUID().toString();
            var uidSystemUser2 = UUID.randomUUID().toString();
            AssignmentRequest assignmentRequest = AssignmentRequest.builder()
                .request(createSystemRoleRequest(PCS_PROCESS_ID, PCS_REFERENCE_HEARINGS))
                .requestedRoles(List.of(
                    createHearingSystemRoleAssignment(uidSystemUser1, ROLE_HEARING_MANAGER, JURISDICTION, null),
                    createHearingSystemRoleAssignment(uidSystemUser1, ROLE_HEARING_VIEWER, JURISDICTION, null),
                    createHearingSystemRoleAssignment(uidSystemUser2, ROLE_HEARING_MANAGER, JURISDICTION, null),
                    createHearingSystemRoleAssignment(uidSystemUser2, ROLE_HEARING_VIEWER, JURISDICTION, null)
                ))
                .build();

            // WHEN
            log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(PCS_CLIENT_ID))
                                                   .content(mapper.writeValueAsBytes(assignmentRequest))
            ).andExpect(status().is(201)).andReturn();

            // THEN
            assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 4);

            // load role assignments
            List<RoleAssignment> roleAssignments1 = assertRoleAssignmentsInDb(uidSystemUser1, 2);
            List<RoleAssignment> roleAssignments2 = assertRoleAssignmentsInDb(uidSystemUser2, 2);

            // verify role-assignment
            assertHearingSystemRoleAssignments(uidSystemUser1, roleAssignments1, JURISDICTION, null);
            assertHearingSystemRoleAssignments(uidSystemUser2, roleAssignments2, JURISDICTION, null);
        }

        @Test
        void shouldRegisterPcsSystemUserRole_Hearing_ReplaceUser() throws Exception {

            // GIVEN
            var uidSystemUserOld = UUID.randomUUID().toString();
            registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUserOld);

            var uidSystemUserNew = UUID.randomUUID().toString();

            // WHEN / THEN
            // REPLACE role assignment for OLD System User with NEW System User
            registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUserNew);

            assertRoleAssignmentsInDb(uidSystemUserOld, 0); // i.e. REMOVED / REPLACED for OLD System User
        }

        @Test
        void shouldDeletePcsSystemUserRole_Hearing() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();
            registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUser);

            // WHEN
            mockMvc.perform(delete(URL_DELETE_ROLES)
                                .contentType(JSON_CONTENT_TYPE)
                                .headers(getHttpHeaders(PCS_CLIENT_ID))
                                .param("process", PCS_PROCESS_ID)
                                .param("reference", PCS_REFERENCE_HEARINGS)
            ).andExpect(status().is(204)).andReturn();

            // THEN
            // verify role assignment removed
            assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. REMOVED for System User
        }

        @Test
        void shouldRejectPcsSystemUserRole_Hearing_ReplaceExistingFalse() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();
            var request = createSystemRoleRequest(PCS_PROCESS_ID, PCS_REFERENCE_HEARINGS);
            request.setReplaceExisting(false); // NB: OVERRIDE replaceExisting to FALSE
            AssignmentRequest assignmentRequest = AssignmentRequest.builder()
                .request(request)
                .requestedRoles(List.of(
                    createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_MANAGER, JURISDICTION, null),
                    createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_VIEWER, JURISDICTION, null)
                ))
                .build();

            // WHEN
            log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(PCS_CLIENT_ID))
                                                   .content(mapper.writeValueAsBytes(assignmentRequest))
            ).andExpect(status().is(422)).andReturn(); // NB: status now 422

            // THEN
            assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 2);

            // load role assignments
            assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
        }

        @Test
        void shouldRejectPcsSystemUserRole_Hearing_WrongJurisdiction() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();
            AssignmentRequest assignmentRequest = AssignmentRequest.builder()
                .request(createSystemRoleRequest(PCS_PROCESS_ID, PCS_REFERENCE_HEARINGS))
                .requestedRoles(List.of(
                    // NB: both have wrong jurisdiction specified
                    createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_MANAGER, JURISDICTION_WRONG, null),
                    createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_VIEWER, JURISDICTION_WRONG, null)
                ))
                .build();

            // WHEN
            log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(PCS_CLIENT_ID))
                                                   .content(mapper.writeValueAsBytes(assignmentRequest))
            ).andExpect(status().is(422)).andReturn(); // NB: status now 422

            // THEN
            assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 2);

            // load role assignments
            assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
        }

        @Test
        void shouldRejectPcsSystemUserRole_Hearing_WrongS2SClientId() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();
            AssignmentRequest assignmentRequest = AssignmentRequest.builder()
                .request(createSystemRoleRequest(PCS_PROCESS_ID, PCS_REFERENCE_HEARINGS))
                .requestedRoles(List.of(
                    createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_MANAGER, JURISDICTION, null),
                    createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_VIEWER, JURISDICTION, null)
                ))
                .build();

            // WHEN
            log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(AUTHORISED_SERVICE)) // NB: WRONG ID
                                                   .content(mapper.writeValueAsBytes(assignmentRequest))
            ).andExpect(status().is(422)).andReturn(); // NB: status now 422

            // THEN
            assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 2);

            // load role assignments
            assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
        }

    }


    private void registerAndVerifyHearingSystemUserRoleAssignments(String uidSystemUser) throws Exception {

        // GIVEN
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(PCS_PROCESS_ID, PCS_REFERENCE_HEARINGS))
            .requestedRoles(List.of(
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_MANAGER, JURISDICTION, null),
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_VIEWER, JURISDICTION, null)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(PCS_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 2);

        // load role assignments
        List<RoleAssignment> roleAssignments = assertRoleAssignmentsInDb(uidSystemUser, 2);

        // verify role-assignment
        assertHearingSystemRoleAssignments(uidSystemUser, roleAssignments, JURISDICTION, null);
    }

}
