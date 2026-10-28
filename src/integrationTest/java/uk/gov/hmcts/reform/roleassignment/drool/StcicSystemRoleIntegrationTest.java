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
public class StcicSystemRoleIntegrationTest extends BaseDroolIntegrationTest {

    static final String STCIC_CLIENT_ID = "sptribs_case_api";
    static final String STCIC_PROCESS_ID = "st-cic-system-user";
    static final String STCIC_REFERENCE_TASK_SUPERVISOR = "st-cic-task-supervisor-system-user";

    static final String JURISDICTION = "ST_CIC";
    static final String JURISDICTION_WRONG = "IA"; // NB: IA have a number of their own System User role assignments


    @Nested
    @DisplayName("TaskSupervisor system roles")
    class TaskSupervisorSystemRoleTests {

        @Test
        void shouldRegisterStCicSystemUserRole_TaskSupervisor_SingleUser() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();

            // WHEN / THEN
            registerAndVerifyTaskSupervisorSystemUserRoleAssignment(uidSystemUser);
        }

        @Test
        void shouldRegisterStCicSystemUserRole_TaskSupervisor_MultipleUser() throws Exception {

            // GIVEN
            var uidSystemUser1 = UUID.randomUUID().toString();
            var uidSystemUser2 = UUID.randomUUID().toString();
            AssignmentRequest assignmentRequest = AssignmentRequest.builder()
                .request(createSystemRoleRequest(STCIC_PROCESS_ID, STCIC_REFERENCE_TASK_SUPERVISOR))
                .requestedRoles(List.of(
                    createSystemRoleAssignment(uidSystemUser1, ROLE_TASK_SUPERVISOR, JURISDICTION),
                    createSystemRoleAssignment(uidSystemUser2, ROLE_TASK_SUPERVISOR, JURISDICTION)
                ))
                .build();

            // WHEN
            log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(STCIC_CLIENT_ID))
                                                   .content(mapper.writeValueAsBytes(assignmentRequest))
            ).andExpect(status().is(201)).andReturn();

            // THEN
            assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 2);

            // load role assignments
            List<RoleAssignment> roleAssignments1 = assertRoleAssignmentsInDb(uidSystemUser1, 1);
            List<RoleAssignment> roleAssignments2 = assertRoleAssignmentsInDb(uidSystemUser2, 1);

            // verify role-assignment
            assertSystemRoleAssignmentDefaultValues(
                uidSystemUser1,
                roleAssignments1,
                ROLE_TASK_SUPERVISOR,
                JURISDICTION
            );
            assertSystemRoleAssignmentDefaultValues(
                uidSystemUser2,
                roleAssignments2,
                ROLE_TASK_SUPERVISOR,
                JURISDICTION
            );
        }

        @Test
        void shouldRegisterStCicSystemUserRole_TaskSupervisor_ReplaceUser() throws Exception {

            // GIVEN
            var uidSystemUserOld = UUID.randomUUID().toString();
            registerAndVerifyTaskSupervisorSystemUserRoleAssignment(uidSystemUserOld);

            var uidSystemUserNew = UUID.randomUUID().toString();

            // WHEN / THEN
            // REPLACE role assignment for OLD System User with NEW System User
            registerAndVerifyTaskSupervisorSystemUserRoleAssignment(uidSystemUserNew);

            assertRoleAssignmentsInDb(uidSystemUserOld, 0); // i.e. REMOVED / REPLACED for OLD System User
        }

        @Test
        void shouldDeleteStCicSystemUserRole_TaskSupervisor() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();
            registerAndVerifyTaskSupervisorSystemUserRoleAssignment(uidSystemUser);

            // WHEN
            mockMvc.perform(delete(URL_DELETE_ROLES)
                                .contentType(JSON_CONTENT_TYPE)
                                .headers(getHttpHeaders(STCIC_CLIENT_ID))
                                .param("process", STCIC_PROCESS_ID)
                                .param("reference", STCIC_REFERENCE_TASK_SUPERVISOR)
            ).andExpect(status().is(204)).andReturn();

            // THEN
            // verify role assignment removed
            assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. REMOVED for System User
        }

        @Test
        void shouldRejectStCicSystemUserRole_TaskSupervisor_ReplaceExistingFalse() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();
            var request = createSystemRoleRequest(STCIC_PROCESS_ID, STCIC_REFERENCE_TASK_SUPERVISOR);
            request.setReplaceExisting(false); // NB: OVERRIDE replaceExisting to FALSE
            AssignmentRequest assignmentRequest = AssignmentRequest.builder()
                .request(request)
                .requestedRoles(List.of(
                    createSystemRoleAssignment(uidSystemUser, ROLE_TASK_SUPERVISOR, JURISDICTION)
                ))
                .build();

            // WHEN
            log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(STCIC_CLIENT_ID))
                                                   .content(mapper.writeValueAsBytes(assignmentRequest))
            ).andExpect(status().is(422)).andReturn(); // NB: status now 422

            // THEN
            assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

            // load role assignments
            assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
        }

        @Test
        void shouldRejectStCicSystemUserRole_TaskSupervisor_WrongJurisdiction() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();
            AssignmentRequest assignmentRequest = AssignmentRequest.builder()
                .request(createSystemRoleRequest(STCIC_PROCESS_ID, STCIC_REFERENCE_TASK_SUPERVISOR))
                .requestedRoles(List.of(
                    // NB: wrong Jurisdiction value
                    createSystemRoleAssignment(uidSystemUser, ROLE_TASK_SUPERVISOR, JURISDICTION_WRONG)
                ))
                .build();

            // WHEN
            log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
            MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                                   .contentType(JSON_CONTENT_TYPE)
                                                   .headers(getHttpHeaders(STCIC_CLIENT_ID))
                                                   .content(mapper.writeValueAsBytes(assignmentRequest))
            ).andExpect(status().is(422)).andReturn(); // NB: status now 422

            // THEN
            assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

            // load role assignments
            assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
        }

        @Test
        void shouldRejectStCicSystemUserRole_TaskSupervisor_WrongS2SClientId() throws Exception {

            // GIVEN
            var uidSystemUser = UUID.randomUUID().toString();
            AssignmentRequest assignmentRequest = AssignmentRequest.builder()
                .request(createSystemRoleRequest(STCIC_PROCESS_ID, STCIC_REFERENCE_TASK_SUPERVISOR))
                .requestedRoles(List.of(
                    createSystemRoleAssignment(uidSystemUser, ROLE_TASK_SUPERVISOR, JURISDICTION)
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
            assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

            // load role assignments
            assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
        }

    }


    private void registerAndVerifyTaskSupervisorSystemUserRoleAssignment(String uidSystemUser) throws Exception {

        // GIVEN
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(STCIC_PROCESS_ID, STCIC_REFERENCE_TASK_SUPERVISOR))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_TASK_SUPERVISOR, JURISDICTION)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(STCIC_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 1);

        // load role assignments
        List<RoleAssignment> roleAssignments = assertRoleAssignmentsInDb(uidSystemUser, 1);

        // verify role-assignment
        assertSystemRoleAssignmentDefaultValues(uidSystemUser, roleAssignments, ROLE_TASK_SUPERVISOR, JURISDICTION);
    }

}
