package uk.gov.hmcts.reform.roleassignment.drool;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
public class CivilSystemRoleIntegrationTest extends BaseDroolIntegrationTest {

    static final String CIVIL_CLIENT_ID = "civil_service";
    static final String CIVIL_PROCESS_ID = "civil-system-user";
    static final String CIVIL_REFERENCE_CBUS = "civil-cbus-system-user";
    static final String CIVIL_REFERENCE_HEARINGS = "civil-hearings-system-user";
    static final String CIVIL_REFERENCE_CASE_ALLOCATOR = "civil-case-allocator-system-user";

    static final String CASE_TYPE = "CIVIL";
    static final String JURISDICTION = "CIVIL";
    static final String JURISDICTION_WRONG = "IA"; // NB: IA have a number of their own System User role assignments

    static final String ROLE_CBUS_SYSTEM_USER = "cbus-system-user";

    // UID from `/src/integrationTest/resources/sql/insert_organisation_role_assignment_civil.sql`
    static final String UID_TRIBUNAL_CASEWORKER = "956efa32-80c4-4628-9e91-0fe0fc4f9c8d";


    // region CaseAllocator system roles

    @Test
    public void shouldRegisterCivilSystemUserRole_CaseAllocator_SingleUser() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();

        // WHEN / THEN
        registerAndVerifyCaseAllocatorSystemUserRoleAssignment(uidSystemUser);
    }

    private void registerAndVerifyCaseAllocatorSystemUserRoleAssignment(String uidSystemUser) throws Exception {

        // GIVEN
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CASE_ALLOCATOR))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_CASE_ALLOCATOR, JURISDICTION)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 1);

        // load role assignments
        List<RoleAssignment> roleAssignments = assertRoleAssignmentsInDb(uidSystemUser, 1);

        // verify role-assignment
        assertSystemRoleAssignmentDefaultValues(uidSystemUser, roleAssignments, ROLE_CASE_ALLOCATOR, JURISDICTION);
    }

    @Test
    public void shouldRegisterCivilSystemUserRole_CaseAllocator_MultipleUser() throws Exception {

        // GIVEN
        var uidSystemUser1 = UUID.randomUUID().toString();
        var uidSystemUser2 = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CASE_ALLOCATOR))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser1, ROLE_CASE_ALLOCATOR, JURISDICTION),
                createSystemRoleAssignment(uidSystemUser2, ROLE_CASE_ALLOCATOR, JURISDICTION)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 2);

        // load role assignments
        List<RoleAssignment> roleAssignments1 = assertRoleAssignmentsInDb(uidSystemUser1, 1);
        List<RoleAssignment> roleAssignments2 = assertRoleAssignmentsInDb(uidSystemUser2, 1);

        // verify role-assignment
        assertSystemRoleAssignmentDefaultValues(uidSystemUser1, roleAssignments1, ROLE_CASE_ALLOCATOR, JURISDICTION);
        assertSystemRoleAssignmentDefaultValues(uidSystemUser2, roleAssignments2, ROLE_CASE_ALLOCATOR, JURISDICTION);
    }

    @Test
    public void shouldRegisterCivilSystemUserRole_CaseAllocator_ReplaceUser() throws Exception {

        // GIVEN
        var uidSystemUserOld = UUID.randomUUID().toString();
        registerAndVerifyCaseAllocatorSystemUserRoleAssignment(uidSystemUserOld);

        var uidSystemUserNew = UUID.randomUUID().toString();

        // WHEN / THEN
        // REPLACE role assignment for OLD System User with NEW System User
        registerAndVerifyCaseAllocatorSystemUserRoleAssignment(uidSystemUserNew);

        assertRoleAssignmentsInDb(uidSystemUserOld, 0); // i.e. REMOVED / REPLACED for OLD System User
    }

    @Test
    public void shouldRegisterCivilSystemUserRole_CaseAllocator_WithoutResettingOtherSystemRoles() throws Exception {

        // GIVEN
        var uidSystemUserCA = UUID.randomUUID().toString();
        var uidSystemUserCbus = UUID.randomUUID().toString();
        var uidSystemUserHearings = UUID.randomUUID().toString();

        registerAndVerifyCbusSystemUserRoleAssignment(uidSystemUserCbus);
        registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUserHearings);

        // WHEN
        registerAndVerifyCaseAllocatorSystemUserRoleAssignment(uidSystemUserCA);

        // THEN
        assertAllSystemUserRolesIntact(uidSystemUserCA, uidSystemUserCbus, uidSystemUserHearings);
    }

    @Test
    public void shouldDeleteCivilSystemUserRole_CaseAllocator() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        registerAndVerifyCaseAllocatorSystemUserRoleAssignment(uidSystemUser);

        // WHEN
        mockMvc.perform(delete(URL_DELETE_ROLES)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                            .param("process", CIVIL_PROCESS_ID)
                            .param("reference", CIVIL_REFERENCE_CASE_ALLOCATOR)
        ).andExpect(status().is(204)).andReturn();

        // THEN
        // verify role assignment removed
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. REMOVED for System User
    }

    @Test
    public void shouldRejectCivilSystemUserRole_CaseAllocator_ReplaceExistingFalse() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        var request = createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CASE_ALLOCATOR);
        request.setReplaceExisting(false); // NB: OVERRIDE replaceExisting to FALSE
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(request)
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_CASE_ALLOCATOR, JURISDICTION)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(422)).andReturn(); // NB: status now 422

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

        // load role assignments
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
    }

    @Test
    public void shouldRejectCivilSystemUserRole_CaseAllocator_WrongJurisdiction() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CASE_ALLOCATOR))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_CASE_ALLOCATOR, JURISDICTION_WRONG) // NB: wrong value
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(422)).andReturn(); // NB: status now 422

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

        // load role assignments
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
    }

    @Test
    public void shouldRejectCivilSystemUserRole_CaseAllocator_WrongS2SClientId() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CASE_ALLOCATOR))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_CASE_ALLOCATOR, JURISDICTION)
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

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
        {"classpath:sql/insert_organisation_role_assignment_civil.sql"}
    )
    public void shouldApproveCaseAllocationForCivilSystemUserRole() throws Exception {

        // GIVEN
        var uidSystemUser = TEST_AUTH_USER_ID; // NB: authenticated user must be the system user
        registerAndVerifyCaseAllocatorSystemUserRoleAssignment(uidSystemUser);

        // NB: stub data in DataStore
        mockRetrieveDataServiceGetCaseById(CASE_ID, "CIVIL", "CIVIL");

        // create case role assignment request
        AssignmentRequest assignmentRequestCaseRole = createCaseRoleAssignmentRequest(uidSystemUser,
                                                                                      UID_TRIBUNAL_CASEWORKER,
                                                                                      RoleCategory.LEGAL_OPERATIONS,
                                                                                      "allocated-legal-adviser",
                                                                                      "CIVIL",
                                                                                      CASE_ID);

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequestCaseRole));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(AUTHORISED_SERVICE_XUI))
                                               .content(mapper.writeValueAsBytes(assignmentRequestCaseRole))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 1);

        // load role assignments
        assertRoleAssignmentsInDb(UID_TRIBUNAL_CASEWORKER, 2);
    }

    // endregion


    // region CBUS system roles

    @Test
    public void shouldRegisterCivilSystemUserRole_CBUS_SingleUser() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();

        // WHEN / THEN
        registerAndVerifyCbusSystemUserRoleAssignment(uidSystemUser);
    }

    private void registerAndVerifyCbusSystemUserRoleAssignment(String uidSystemUser) throws Exception {

        // GIVEN
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CBUS))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_CBUS_SYSTEM_USER, JURISDICTION)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 1);

        // load role assignments
        List<RoleAssignment> roleAssignments = assertRoleAssignmentsInDb(uidSystemUser, 1);

        // verify role-assignment
        assertSystemRoleAssignmentDefaultValues(uidSystemUser, roleAssignments, ROLE_CBUS_SYSTEM_USER, JURISDICTION);
    }

    @Test
    public void shouldRegisterCivilSystemUserRole_CBUS_MultipleUser() throws Exception {

        // GIVEN
        var uidSystemUser1 = UUID.randomUUID().toString();
        var uidSystemUser2 = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CBUS))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser1, ROLE_CBUS_SYSTEM_USER, JURISDICTION),
                createSystemRoleAssignment(uidSystemUser2, ROLE_CBUS_SYSTEM_USER, JURISDICTION)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 2);

        // load role assignments
        List<RoleAssignment> roleAssignments1 = assertRoleAssignmentsInDb(uidSystemUser1, 1);
        List<RoleAssignment> roleAssignments2 = assertRoleAssignmentsInDb(uidSystemUser2, 1);

        // verify role-assignment
        assertSystemRoleAssignmentDefaultValues(uidSystemUser1, roleAssignments1, ROLE_CBUS_SYSTEM_USER, JURISDICTION);
        assertSystemRoleAssignmentDefaultValues(uidSystemUser2, roleAssignments2, ROLE_CBUS_SYSTEM_USER, JURISDICTION);
    }

    @Test
    public void shouldRegisterCivilSystemUserRole_CBUS_ReplaceUser() throws Exception {

        // GIVEN
        var uidSystemUserOld = UUID.randomUUID().toString();
        registerAndVerifyCbusSystemUserRoleAssignment(uidSystemUserOld);

        var uidSystemUserNew = UUID.randomUUID().toString();

        // WHEN / THEN
        // REPLACE role assignment for OLD System User with NEW System User
        registerAndVerifyCbusSystemUserRoleAssignment(uidSystemUserNew);

        assertRoleAssignmentsInDb(uidSystemUserOld, 0); // i.e. REMOVED / REPLACED for OLD System User
    }

    @Test
    public void shouldRegisterCivilSystemUserRole_CBUS_WithoutResettingOtherSystemRoles() throws Exception {

        // GIVEN
        var uidSystemUserCA = UUID.randomUUID().toString();
        var uidSystemUserCbus = UUID.randomUUID().toString();
        var uidSystemUserHearings = UUID.randomUUID().toString();

        registerAndVerifyCaseAllocatorSystemUserRoleAssignment(uidSystemUserCA);
        registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUserHearings);

        // WHEN
        registerAndVerifyCbusSystemUserRoleAssignment(uidSystemUserCbus);

        // THEN
        assertAllSystemUserRolesIntact(uidSystemUserCA, uidSystemUserCbus, uidSystemUserHearings);
    }

    @Test
    public void shouldDeleteCivilSystemUserRole_CBUS() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        registerAndVerifyCbusSystemUserRoleAssignment(uidSystemUser);

        // WHEN
        mockMvc.perform(delete(URL_DELETE_ROLES)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                            .param("process", CIVIL_PROCESS_ID)
                            .param("reference", CIVIL_REFERENCE_CBUS)
        ).andExpect(status().is(204)).andReturn();

        // THEN
        // verify role assignment removed
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. REMOVED for System User
    }

    @Test
    public void shouldRejectCivilSystemUserRole_CBUS_ReplaceExistingFalse() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        var request = createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CBUS);
        request.setReplaceExisting(false); // NB: OVERRIDE replaceExisting to FALSE
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(request)
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_CBUS_SYSTEM_USER, JURISDICTION)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(422)).andReturn(); // NB: status now 422

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

        // load role assignments
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
    }

    @Test
    public void shouldRejectCivilSystemUserRole_CBUS_WrongJurisdiction() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CBUS))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_CBUS_SYSTEM_USER, JURISDICTION_WRONG) // NB: wrong value
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(422)).andReturn(); // NB: status now 422

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 1);

        // load role assignments
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
    }

    @Test
    public void shouldRejectCivilSystemUserRole_CBUS_WrongS2SClientId() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_CBUS))
            .requestedRoles(List.of(
                createSystemRoleAssignment(uidSystemUser, ROLE_CBUS_SYSTEM_USER, JURISDICTION)
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

    // endregion


    // region Hearing system roles

    @Test
    public void shouldRegisterCivilSystemUserRole_Hearing_SingleUser() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();

        // WHEN / THEN
        registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUser);
    }

    private void registerAndVerifyHearingSystemUserRoleAssignments(String uidSystemUser) throws Exception {

        // GIVEN
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_HEARINGS))
            .requestedRoles(List.of(
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_MANAGER, JURISDICTION, CASE_TYPE),
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_VIEWER, JURISDICTION, CASE_TYPE)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 2);

        // load role assignments
        List<RoleAssignment> roleAssignments = assertRoleAssignmentsInDb(uidSystemUser, 2);

        // verify role-assignment
        assertHearingSystemRoleAssignments(uidSystemUser, roleAssignments, JURISDICTION, CASE_TYPE);
    }

    @Test
    public void shouldRegisterCivilSystemUserRole_Hearing_MultipleUser() throws Exception {

        // GIVEN
        var uidSystemUser1 = UUID.randomUUID().toString();
        var uidSystemUser2 = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_HEARINGS))
            .requestedRoles(List.of(
                createHearingSystemRoleAssignment(uidSystemUser1, ROLE_HEARING_MANAGER, JURISDICTION, CASE_TYPE),
                createHearingSystemRoleAssignment(uidSystemUser1, ROLE_HEARING_VIEWER, JURISDICTION, CASE_TYPE),
                createHearingSystemRoleAssignment(uidSystemUser2, ROLE_HEARING_MANAGER, JURISDICTION, CASE_TYPE),
                createHearingSystemRoleAssignment(uidSystemUser2, ROLE_HEARING_VIEWER, JURISDICTION, CASE_TYPE)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(201)).andReturn();

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.APPROVED, result, 4);

        // load role assignments
        List<RoleAssignment> roleAssignments1 = assertRoleAssignmentsInDb(uidSystemUser1, 2);
        List<RoleAssignment> roleAssignments2 = assertRoleAssignmentsInDb(uidSystemUser2, 2);

        // verify role-assignment
        assertHearingSystemRoleAssignments(uidSystemUser1, roleAssignments1, JURISDICTION, CASE_TYPE);
        assertHearingSystemRoleAssignments(uidSystemUser2, roleAssignments2, JURISDICTION, CASE_TYPE);
    }

    @Test
    public void shouldRegisterCivilSystemUserRole_Hearing_ReplaceUser() throws Exception {

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
    public void shouldRegisterCivilSystemUserRole_Hearing_WithoutResettingOtherSystemRoles() throws Exception {

        // GIVEN
        var uidSystemUserCA = UUID.randomUUID().toString();
        var uidSystemUserCbus = UUID.randomUUID().toString();
        var uidSystemUserHearings = UUID.randomUUID().toString();

        registerAndVerifyCaseAllocatorSystemUserRoleAssignment(uidSystemUserCA);
        registerAndVerifyCbusSystemUserRoleAssignment(uidSystemUserCbus);

        // WHEN
        registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUserHearings);

        // THEN
        assertAllSystemUserRolesIntact(uidSystemUserCA, uidSystemUserCbus, uidSystemUserHearings);
    }

    @Test
    public void shouldDeleteCivilSystemUserRole_Hearing() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        registerAndVerifyHearingSystemUserRoleAssignments(uidSystemUser);

        // WHEN
        mockMvc.perform(delete(URL_DELETE_ROLES)
                            .contentType(JSON_CONTENT_TYPE)
                            .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                            .param("process", CIVIL_PROCESS_ID)
                            .param("reference", CIVIL_REFERENCE_HEARINGS)
        ).andExpect(status().is(204)).andReturn();

        // THEN
        // verify role assignment removed
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. REMOVED for System User
    }

    @Test
    public void shouldRejectCivilSystemUserRole_Hearing_ReplaceExistingFalse() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        var request = createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_HEARINGS);
        request.setReplaceExisting(false); // NB: OVERRIDE replaceExisting to FALSE
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(request)
            .requestedRoles(List.of(
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_MANAGER, JURISDICTION, CASE_TYPE),
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_VIEWER, JURISDICTION, CASE_TYPE)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(422)).andReturn(); // NB: status now 422

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 2);

        // load role assignments
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
    }

    @Test
    public void shouldRejectCivilSystemUserRole_Hearing_WrongJurisdiction() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_HEARINGS))
            .requestedRoles(List.of(
                // NB: both have wrong jurisdiction specified
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_MANAGER, JURISDICTION_WRONG, CASE_TYPE),
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_VIEWER, JURISDICTION_WRONG, CASE_TYPE)
            ))
            .build();

        // WHEN
        log.info("Create RoleAssignment Request: {}", writeValueAsPrettyJson(assignmentRequest));
        MvcResult result = mockMvc.perform(post(URL_CREATE_ROLES)
                                               .contentType(JSON_CONTENT_TYPE)
                                               .headers(getHttpHeaders(CIVIL_CLIENT_ID))
                                               .content(mapper.writeValueAsBytes(assignmentRequest))
        ).andExpect(status().is(422)).andReturn(); // NB: status now 422

        // THEN
        assertCreateRoleAssignmentResponseStatus(Status.REJECTED, result, 2);

        // load role assignments
        assertRoleAssignmentsInDb(uidSystemUser, 0); // i.e. NOT created
    }

    @Test
    public void shouldRejectCivilSystemUserRole_Hearing_WrongS2SClientId() throws Exception {

        // GIVEN
        var uidSystemUser = UUID.randomUUID().toString();
        AssignmentRequest assignmentRequest = AssignmentRequest.builder()
            .request(createSystemRoleRequest(CIVIL_PROCESS_ID, CIVIL_REFERENCE_HEARINGS))
            .requestedRoles(List.of(
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_MANAGER, JURISDICTION, CASE_TYPE),
                createHearingSystemRoleAssignment(uidSystemUser, ROLE_HEARING_VIEWER, JURISDICTION, CASE_TYPE)
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

    // endregion

    private void assertAllSystemUserRolesIntact(String uidSystemUserCA,
                                                String uidSystemUserCbus,
                                                String uidSystemUserHearings) {

        List<RoleAssignment> roleAssignmentsCA = assertRoleAssignmentsInDb(uidSystemUserCA, 1);
        List<RoleAssignment> roleAssignmentsCbus = assertRoleAssignmentsInDb(uidSystemUserCbus, 1);
        List<RoleAssignment> roleAssignmentsHearings = assertRoleAssignmentsInDb(uidSystemUserHearings, 2);

        assertSystemRoleAssignmentDefaultValues(
            uidSystemUserCA, roleAssignmentsCA, ROLE_CASE_ALLOCATOR, JURISDICTION
        );
        assertSystemRoleAssignmentDefaultValues(
            uidSystemUserCbus, roleAssignmentsCbus, ROLE_CBUS_SYSTEM_USER, JURISDICTION
        );
        assertHearingSystemRoleAssignments(
            uidSystemUserHearings, roleAssignmentsHearings, JURISDICTION, CASE_TYPE
        );
    }

}
