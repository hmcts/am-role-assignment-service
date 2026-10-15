package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.BaseTest;
import uk.gov.hmcts.reform.roleassignment.MockUtils;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfigRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.createQueryRequest;

@TestPropertySource(properties = {"ras.environment=pr"})
class RoleAssignmentIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentIntegrationTest.class);
    private static final String COUNT_HISTORY_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment_history";

    private static final String GET_ASSIGNMENT_STATUS_QUERY = "SELECT actor_id FROM role_assignment where id = ?";
    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    private static final String CASE_ID = "1234567890123456";
    private static final String ROLE_ASSIGNMENT_ID = "2ef8ebf3-266e-45d3-a3b8-4ce1e5d93b9f";
    private static final String ROLE_TYPE_CASE = RoleType.CASE.toString();
    private static final String ROLE_TYPE_ORG = RoleType.ORGANISATION.toString();

    private static final String URL_GET_ROLES = "/am/role-assignments/roles";
    private static final String URL_GET_ROLE_ASSIGNMENTS_FOR_ACTOR = "/am/role-assignments/actors/";
    private static final String URL_QUERY_ROLE_ASSIGNMENTS = "/am/role-assignments/query";

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    @MockBean
    private IdamApi idamApi;

    @Autowired
    private DataSource ds;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @MockBean
    private IdamRoleService idamRoleService;

    @BeforeEach
    void setUp() throws Exception {
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.openMocks(this);
        UserInfo userInfo = UserInfo.builder()
            .uid(ACTOR_ID)
            .sub("emailId@a.com")
            .build();
        doReturn(userInfo).when(idamApi).retrieveUserInfo(anyString());
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_role_assignment_request.sql",
        "classpath:sql/insert_role_assignment_history.sql"
    })
    void shouldGetRecordCountFromHistoryTable() {
        final int count = template.queryForObject(COUNT_HISTORY_RECORDS_QUERY, Integer.class);
        logger.info(" Total number of records fetched from role assignment history table...{}", count);
        assertEquals(15, count, "role_assignment_history record count ");
    }

    @Test
    void disableTestAsPerFlagValue() {
        assertRoleAssignmentRecordSize();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    void shouldGetRecordsFromRoleAssignmentTable() {
        assertRoleAssignmentRecordSize();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
        {"classpath:sql/insert_role_assignment.sql",
            "classpath:sql/insert_actor_cache_control.sql"})
    void shouldGetRoleAssignmentsBasedOnActorId() throws Exception {
        assertRoleAssignmentRecordSize();
        final var url = URL_GET_ROLE_ASSIGNMENTS_FOR_ACTOR + ACTOR_ID;

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders()))
            .andExpect(status().is(200))
            .andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(1, existingRoleAssignments.size());
        assertEquals(
            ROLE_ASSIGNMENT_ID,
            existingRoleAssignments.get(0).getId().toString()
        );
        assertEquals(
            ACTOR_ID,
            existingRoleAssignments.get(0).getActorId()
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    void shouldGetRoleAssignmentsBasedOnRoleTypeAndActorId() throws Exception {
        assertRoleAssignmentRecordSize();

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(Collections.singletonList(ACTOR_ID))
            .roleType(Collections.singletonList(ROLE_TYPE_CASE))
            .build();

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(queryRequest))
        )
            .andExpect(status().is(200))
            .andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(1, existingRoleAssignments.size());
        assertEquals(
            ROLE_ASSIGNMENT_ID,
            existingRoleAssignments.get(0).getId().toString()
        );
        assertEquals(
            ACTOR_ID,
            existingRoleAssignments.get(0).getActorId()
        );
        assertEquals(
            ROLE_TYPE_CASE,
            existingRoleAssignments.get(0).getRoleType().toString()
        );
    }


    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    void shouldGetRoleAssignmentsBasedOnRoleTypeAndCaseId() throws Exception {
        assertRoleAssignmentRecordSize();

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("caseId", Collections.singletonList(CASE_ID));

        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(Collections.singletonList(ROLE_TYPE_CASE))
            .attributes(attributes)
            .build();

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(queryRequest))
        )
            .andExpect(status().is(200))
            .andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(1, existingRoleAssignments.size());
        assertEquals(
            ROLE_ASSIGNMENT_ID,
            existingRoleAssignments.get(0).getId().toString()
        );
        assertEquals(
            ACTOR_ID,
            existingRoleAssignments.get(0).getActorId()
        );
        assertEquals(
            ROLE_TYPE_CASE,
            existingRoleAssignments.get(0).getRoleType().toString()
        );
        assertEquals(
            CASE_ID,
            existingRoleAssignments.get(0).getAttributes().get("caseId").asText()
        );
    }

    @Test
    void shouldGetListOfRoles() throws Exception {

        final MvcResult result = mockMvc.perform(get(URL_GET_ROLES)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
        )
            .andExpect(status().is(200))
            .andReturn();
        var response = result.getResponse().getContentAsString();

        List<RoleConfigRole> roleConfigRoles = mapper.readValue(response, new TypeReference<>() {
        });

        assertEquals(282, roleConfigRoles.size());
        assertThat(roleConfigRoles, containsInAnyOrder(
            roleMatcher("hrs-team-leader", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("hrs-listener", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("hrs-sharer", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("district-judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("deputy-district-judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("recorder", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("allocated-nbc-caseworker", RoleCategory.ADMIN, RoleType.CASE, true),
            roleMatcher("gatekeeping-judge", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("fl401-judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("duty-advisor-request", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("claimant", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("claimant-solicitor", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("defendant-solicitor", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-create", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, false),
            roleMatcher("solicitor-respa", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-respb", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-respc", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-respd", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-respe", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-respf", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-respg", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-resph", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-respi", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-respj", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childa", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childb", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childc", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childd", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childe", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childf", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childg", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childh", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childi", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childj", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childk", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childl", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childm", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childn", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-childo", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("solicitor-epsm", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("la-create", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, false),
            roleMatcher("la-primary", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("la-secondary", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("la-mla", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("bailiff-admin", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("allocated-bailiff", RoleCategory.ADMIN, RoleType.CASE, true),
            roleMatcher("allocated-wlu-caseworker", RoleCategory.ADMIN, RoleType.CASE, true),
            roleMatcher("bailiff", RoleCategory.ENFORCEMENT, RoleType.ORGANISATION, true),
            roleMatcher("bailiff-manager", RoleCategory.ENFORCEMENT, RoleType.ORGANISATION, true),
            roleMatcher("hmcts-enforcement", RoleCategory.ENFORCEMENT, RoleType.ORGANISATION, false),
            roleMatcher("case-allocator", RoleCategory.JUDICIAL, RoleType.ORGANISATION, false),
            roleMatcher("case-allocator", RoleCategory.JUDICIAL, RoleType.CASE, false),
            roleMatcher("case-allocator", RoleCategory.LEGAL_OPERATIONS, RoleType.ORGANISATION, false),
            roleMatcher("case-allocator", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("case-allocator", RoleCategory.ADMIN, RoleType.ORGANISATION, false),
            roleMatcher("case-allocator", RoleCategory.ADMIN, RoleType.CASE, false),
            roleMatcher("case-allocator", RoleCategory.SYSTEM, RoleType.ORGANISATION, false),
            roleMatcher("task-supervisor", RoleCategory.JUDICIAL, RoleType.ORGANISATION, false),
            roleMatcher("task-supervisor", RoleCategory.LEGAL_OPERATIONS, RoleType.ORGANISATION, false),
            roleMatcher("task-supervisor", RoleCategory.ADMIN, RoleType.ORGANISATION, false),
            roleMatcher("task-supervisor", RoleCategory.CTSC, RoleType.ORGANISATION, false),
            roleMatcher("case-allocator", RoleCategory.CTSC, RoleType.ORGANISATION, false),
            roleMatcher("case-allocator", RoleCategory.CTSC, RoleType.CASE, false),
            roleMatcher("hmcts-judiciary", RoleCategory.JUDICIAL, RoleType.ORGANISATION, false),
            roleMatcher("hmcts-legal-operations", RoleCategory.LEGAL_OPERATIONS, RoleType.ORGANISATION, false),
            roleMatcher("hmcts-admin", RoleCategory.ADMIN, RoleType.ORGANISATION, false),
            roleMatcher("judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("fee-paid-judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, false),
            roleMatcher("hearing-judge", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("allocated-magistrate", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("leadership-judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("tribunal-caseworker", RoleCategory.LEGAL_OPERATIONS, RoleType.ORGANISATION, true),
            roleMatcher("tribunal-caseworker", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("conflict-of-interest", RoleCategory.JUDICIAL, RoleType.CASE, false),
            roleMatcher("conflict-of-interest", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("conflict-of-interest", RoleCategory.ADMIN, RoleType.CASE, false),
            roleMatcher("conflict-of-interest", RoleCategory.CTSC, RoleType.CASE, false),
            roleMatcher("specific-access-judiciary", RoleCategory.JUDICIAL, RoleType.CASE, false),
            roleMatcher("specific-access-requested", RoleCategory.JUDICIAL, RoleType.CASE, false),
            roleMatcher("specific-access-granted", RoleCategory.JUDICIAL, RoleType.CASE, false),
            roleMatcher("specific-access-denied", RoleCategory.JUDICIAL, RoleType.CASE, false),
            roleMatcher("specific-access-legal-ops", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("specific-access-requested", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("specific-access-granted", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("specific-access-denied", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("specific-access-admin", RoleCategory.ADMIN, RoleType.CASE, false),
            roleMatcher("specific-access-ctsc", RoleCategory.CTSC, RoleType.CASE, false),
            roleMatcher("specific-access-requested", RoleCategory.CTSC, RoleType.CASE, false),
            roleMatcher("specific-access-granted", RoleCategory.CTSC, RoleType.CASE, false),
            roleMatcher("specific-access-denied", RoleCategory.CTSC, RoleType.CASE, false),
            roleMatcher("specific-access-requested", RoleCategory.ADMIN, RoleType.CASE, false),
            roleMatcher("specific-access-granted", RoleCategory.ADMIN, RoleType.CASE, false),
            roleMatcher("specific-access-denied", RoleCategory.ADMIN, RoleType.CASE, false),
            roleMatcher("challenged-access-judiciary", RoleCategory.JUDICIAL, RoleType.CASE, false),
            roleMatcher("challenged-access-legal-ops", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("challenged-access-admin", RoleCategory.ADMIN, RoleType.CASE, false),
            roleMatcher("challenged-access-ctsc", RoleCategory.CTSC, RoleType.CASE, false),
            roleMatcher("hearing-centre-admin", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("national-business-centre", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("nbc-team-leader", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("ctsc", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("hearing-manager", RoleCategory.JUDICIAL, RoleType.ORGANISATION, false),
            roleMatcher("hearing-manager", RoleCategory.LEGAL_OPERATIONS, RoleType.ORGANISATION, false),
            roleMatcher("hearing-manager", RoleCategory.ADMIN, RoleType.ORGANISATION, false),
            roleMatcher("hearing-manager", RoleCategory.CTSC, RoleType.ORGANISATION, false),
            roleMatcher("hearing-manager", RoleCategory.SYSTEM, RoleType.ORGANISATION, false),
            roleMatcher("hearing-viewer", RoleCategory.SYSTEM, RoleType.ORGANISATION, false),
            roleMatcher("hearing-viewer", RoleCategory.JUDICIAL, RoleType.ORGANISATION, false),
            roleMatcher("hearing-viewer", RoleCategory.LEGAL_OPERATIONS, RoleType.ORGANISATION, false),
            roleMatcher("hearing-viewer", RoleCategory.ADMIN, RoleType.ORGANISATION, false),
            roleMatcher("hearing-viewer", RoleCategory.CTSC, RoleType.ORGANISATION, false),
            roleMatcher("listed-hearing-viewer", RoleCategory.OTHER_GOV_DEPT, RoleType.ORGANISATION, false),
            roleMatcher(
                "caseworker-privatelaw-externaluser-viewonly",
                RoleCategory.OTHER_GOV_DEPT,
                RoleType.ORGANISATION,
                false
            ),
            roleMatcher("hearing-centre-team-leader", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("senior-tribunal-caseworker", RoleCategory.LEGAL_OPERATIONS, RoleType.ORGANISATION, true),
            roleMatcher("circuit-judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("specific-access-approver-judiciary", RoleCategory.JUDICIAL, RoleType.ORGANISATION, false),
            roleMatcher("specific-access-approver-judiciary", RoleCategory.CTSC, RoleType.ORGANISATION, false),
            roleMatcher(
                "specific-access-approver-legal-ops",
                RoleCategory.LEGAL_OPERATIONS,
                RoleType.ORGANISATION,
                false
            ),
            roleMatcher("specific-access-approver-legal-ops", RoleCategory.ADMIN, RoleType.ORGANISATION, false),
            roleMatcher("specific-access-approver-legal-ops", RoleCategory.JUDICIAL, RoleType.ORGANISATION, false),
            roleMatcher("specific-access-approver-admin", RoleCategory.ADMIN, RoleType.ORGANISATION, false),
            roleMatcher("specific-access-approver-admin", RoleCategory.CTSC, RoleType.ORGANISATION, false),
            roleMatcher("magistrate", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("specific-access-approver-ctsc", RoleCategory.CTSC, RoleType.ORGANISATION, false),
            roleMatcher("specific-access-approver-ctsc", RoleCategory.ADMIN, RoleType.ORGANISATION, false),
            roleMatcher("ctsc", RoleCategory.CTSC, RoleType.ORGANISATION, true),
            roleMatcher("hmcts-ctsc", RoleCategory.CTSC, RoleType.ORGANISATION, false),
            roleMatcher("ctsc-team-leader", RoleCategory.CTSC, RoleType.ORGANISATION, true),
            roleMatcher("allocated-judge", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("allocated-legal-adviser", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, true),
            roleMatcher("hearing-legal-adviser", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, true),
            roleMatcher("lead-judge", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("tribunal-member-1", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("tribunal-member-2", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("tribunal-member-3", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("allocated-tribunal-caseworker", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, true),
            roleMatcher("allocated-admin-caseworker", RoleCategory.ADMIN, RoleType.CASE, true),
            roleMatcher("allocated-ctsc-caseworker", RoleCategory.CTSC, RoleType.CASE, true),
            roleMatcher("clerk", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("regional-centre-team-leader", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("regional-centre-admin", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("senior-judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("medical", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("fee-paid-medical", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("fee-paid-disability", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("fee-paid-financial", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("fee-paid-tribunal-member", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("interloc-judge", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("appraiser-1", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("appraiser-2", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("cbus-system-user", RoleCategory.SYSTEM, RoleType.ORGANISATION, false),
            roleMatcher("wlu-admin", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("wlu-team-leader", RoleCategory.ADMIN, RoleType.ORGANISATION, true),
            roleMatcher("PRM_Test_GA_Role", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("PRM_Test_Org_Role", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("CaseProfessionalGroupAccess_GA_Role", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("CaseProfessionalGroupAccess_Org_Role", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("Role1", RoleCategory.PROFESSIONAL, RoleType.ORGANISATION, true),
            roleMatcher("cica", RoleCategory.OTHER_GOV_DEPT, RoleType.ORGANISATION, true),
            roleMatcher("allocated-legal-officer", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, true),
            roleMatcher("allocated-administrator", RoleCategory.ADMIN, RoleType.CASE, true),
            roleMatcher("[PETSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[RESPSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[BARRISTER]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CAFCASSSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[EPSMANAGING]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[LABARRISTER]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[LAMANAGING]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[LASOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[LASOCIALWORKER]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORA]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORB]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORC]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORD]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORE]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORF]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORG]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORH]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORI]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[SOLICITORJ]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[LEGALREPRESENTATIVE]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CREATOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, false),
            roleMatcher("[CREATOR]", RoleCategory.CITIZEN, RoleType.CASE, false),
            roleMatcher("[CREATOR]", RoleCategory.JUDICIAL, RoleType.CASE, false),
            roleMatcher("[CREATOR]", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, false),
            roleMatcher("[APPSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORA]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORB]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORC]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORD]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORE]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORF]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORG]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORH]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORI]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORJ]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORK]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORL]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORM]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORN]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CHILDSOLICITORO]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[LASHARED]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[APPLICANTSOLICITORONE]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[APPLICANTSOLICITORTWO]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[RESPONDENTSOLICITORONE]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[RESPONDENTSOLICITORTWO]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[CLAIMANT]", RoleCategory.CITIZEN, RoleType.CASE, false),
            roleMatcher("[CLAIMANTSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[DEFENDANT]", RoleCategory.CITIZEN, RoleType.CASE, false),
            roleMatcher("[DEFENDANTSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[APPLICANTSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[OTHERPARTYSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[RESPONDENTSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[APPONESOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[APPTWOSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[APPLICANTTWO]", RoleCategory.CITIZEN, RoleType.CASE, false),
            roleMatcher("[APPLICANTSOLICITORTWOSPEC]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[APPBARRISTER]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[RESPBARRISTER]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[INTVRSOLICITOR1]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[INTVRSOLICITOR2]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[INTVRSOLICITOR3]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[INTVRSOLICITOR4]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[INTVRBARRISTER1]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[INTVRBARRISTER2]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[INTVRBARRISTER3]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[INTVRBARRISTER4]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTSOLICITOR1]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTSOLICITOR2]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTSOLICITOR3]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTSOLICITOR4]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTSOLICITOR5]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[FL401APPLICANTSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100CHILDSOLICITOR1]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100CHILDSOLICITOR2]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100CHILDSOLICITOR3]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100CHILDSOLICITOR4]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100CHILDSOLICITOR5]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTSOLICITOR1]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTSOLICITOR2]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTSOLICITOR3]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTSOLICITOR4]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTSOLICITOR5]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[FL401RESPONDENTSOLICITOR]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTBARRISTER1]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTBARRISTER2]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTBARRISTER3]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTBARRISTER4]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100APPLICANTBARRISTER5]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[FL401APPLICANTBARRISTER]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTBARRISTER1]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTBARRISTER2]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTBARRISTER3]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTBARRISTER4]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[C100RESPONDENTBARRISTER5]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[FL401RESPONDENTBARRISTER]", RoleCategory.PROFESSIONAL, RoleType.CASE, true),
            roleMatcher("[APPLICANT]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENT]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[CLAIMANTNONLEGALREPRESENTATIVE]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE0]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE1]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE2]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE3]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE4]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE5]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE6]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE7]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE8]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("[RESPONDENTNONLEGALREPRESENTATIVE9]", RoleCategory.CITIZEN, RoleType.CASE, true),
            roleMatcher("post-hearing-judge", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("registrar", RoleCategory.LEGAL_OPERATIONS, RoleType.ORGANISATION, true),
            roleMatcher("registrar", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, true),
            roleMatcher("dwp", RoleCategory.OTHER_GOV_DEPT, RoleType.ORGANISATION, true),
            roleMatcher("hmrc", RoleCategory.OTHER_GOV_DEPT, RoleType.ORGANISATION, true),
            roleMatcher("ibca", RoleCategory.OTHER_GOV_DEPT, RoleType.ORGANISATION, true),
            roleMatcher("post-hearing-salaried-judge", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("tribunal-member", RoleCategory.JUDICIAL, RoleType.ORGANISATION, true),
            roleMatcher("ftpa-judge", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("hearing-panel-judge", RoleCategory.JUDICIAL, RoleType.CASE, true),
            roleMatcher("case-manager", RoleCategory.LEGAL_OPERATIONS, RoleType.CASE, true)
        ));
    }

    private static Matcher<RoleConfigRole> roleMatcher(
        String name, RoleCategory category, RoleType type, boolean substantive) {
        return allOf(
            hasProperty("name", equalTo(name)),
            hasProperty("category", equalTo(category)),
            hasProperty("type", equalTo(type)),
            hasProperty("substantive", equalTo(substantive)),
            hasProperty("label", notNullValue())
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    void shouldGetRoleAssignmentsRecordsBasedOnDynamicQuery() throws Exception {

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("jurisdiction", Collections.singletonList("divorce"));

        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(Collections.singletonList(ROLE_TYPE_ORG))
            .roleName(Collections.singletonList("judge"))
            .actorId(Collections.singletonList("123e4567-e89b-42d3-a456-556642445613"))
            .attributes(attributes)
            .build();

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(queryRequest))
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(3, existingRoleAssignments.size());
        existingRoleAssignments.forEach(element -> assertAll(
            () -> assertEquals(ROLE_TYPE_ORG, element.getRoleType().toString()),
            () -> assertEquals("judge", element.getRoleName()),
            () -> assertEquals("123e4567-e89b-42d3-a456-556642445613", element.getActorId()),
            () -> assertEquals("STANDARD", element.getGrantType().toString()),
            () -> assertEquals("PUBLIC", element.getClassification().toString())
        ));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    void shouldGetEmptyRoleAssignmentsRecordsBasedOnDynamicQuery() throws Exception {
        QueryRequest queryRequest = createQueryRequest(List.of("123e4567-e89b-42d3-a456-556642445612"));

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(queryRequest))
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(0, existingRoleAssignments.size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    void shouldGetRoleAssignmentsFilteredOnAuthorisations() throws Exception {
        QueryRequest queryRequest = createQueryRequest(List.of("f7ec3783-5d5b-4797-bdcd-74908ef1e553"));

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(queryRequest))
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(1, existingRoleAssignments.size());
        assertEquals(List.of("dev", "auth2"), existingRoleAssignments.getFirst().getAuthorisations());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    void shouldGetMultipleRoleAssignmentsOnDifferentAuthorisations() throws Exception {
        QueryRequest queryRequest = QueryRequest.builder().authorisations(List.of("auth2", "auth4", "dev")).build();
        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(queryRequest))
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(2, existingRoleAssignments.size());
        assertEquals(List.of("dev", "auth2"), existingRoleAssignments.getFirst().getAuthorisations());
        assertEquals(List.of("auth3", "auth4"), existingRoleAssignments.get(1).getAuthorisations());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    void shouldNotGetRoleAssignmentsOnNonMatchingActorIdWithMatchingAuthorisations() throws Exception {
        QueryRequest queryRequest = QueryRequest.builder()
            .actorId("bc6fc79a-63ff-4fb4-9780-a935ca9c1ec7")
            .authorisations(List.of("auth2", "auth4", "dev"))
            .build();

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders())
                                                     .content(mapper.writeValueAsBytes(queryRequest))
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(0, existingRoleAssignments.size());
    }

    private void assertRoleAssignmentRecordSize() {
        final Object[] assignmentId = new Object[]{
            ROLE_ASSIGNMENT_ID
        };
        var actorId = template.queryForObject(GET_ASSIGNMENT_STATUS_QUERY, assignmentId, String.class);
        logger.info(" Role assignment actor id is...{}", actorId);
        assertEquals(ACTOR_ID, actorId, "Role assignment actor Id");
    }

    private List<ExistingRoleAssignment> getExistingRoleAssignmentFromMvcResult(MvcResult result)
        throws UnsupportedEncodingException, JsonProcessingException {
        JsonNode jsonResponse = mapper.readValue(result.getResponse().getContentAsString(), JsonNode.class);
        assertNotNull(jsonResponse.get("roleAssignmentResponse"));
        return mapper.readValue(
            jsonResponse.get("roleAssignmentResponse").toString(),
            new TypeReference<>() {
            }
        );
    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }


}
