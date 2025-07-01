package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignment.versions.V2;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertEquals(201, roleConfigRoles.size());
        for (RoleConfigRole roleConfigRole : roleConfigRoles) {
            assertNotNull(roleConfigRole.getName());
            assertNotNull(roleConfigRole.getCategory());
            assertNotNull(roleConfigRole.getType());
            assertNotNull(roleConfigRole.getLabel());
        }
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("queryProvider")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_role_assignment_servicetest.sql"})
    void sampleTest(String testName, String queryJson, String expectedRoleAssignments) throws Exception {
        List<String> expectedRoleIds = Arrays.asList(expectedRoleAssignments.split(","));

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders())
                                                     .content(queryJson.getBytes())
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(expectedRoleIds.size(), existingRoleAssignments.size());
        existingRoleAssignments.forEach(element ->
                                            assertTrue(expectedRoleIds.contains(element.getId().toString()))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("queryProviderV2")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_role_assignment_servicetest.sql"})
    void sampleTestV2(String testName, String queryJson, String expectedRoleAssignments) throws Exception {
        List<String> expectedRoleIds = Arrays.asList(expectedRoleAssignments.split(","));

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(V2.MediaType.POST_ASSIGNMENTS)
                                                     .headers(getHttpHeaders())
                                                     .content(queryJson.getBytes())
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(expectedRoleIds.size(), existingRoleAssignments.size());
        existingRoleAssignments.forEach(element ->
                                            assertTrue(expectedRoleIds.contains(element.getId().toString()))
        );
    }

    private static Stream<Arguments> queryProvider() {
        return Stream.of(
            Arguments.of("single actorId", "{\"actorId\":[\"1001\"]}",
                         "638e8e7a-7d7c-4027-9d53-100000000001"),

            Arguments.of("multiple actorId", "{\"actorId\":[\"1001\",\"1002\"]}",
                         "638e8e7a-7d7c-4027-9d53-100000000001,638e8e7a-7d7c-4027-9d53-100000000002," +
                             "638e8e7a-7d7c-4027-9d53-100000000003"),

            Arguments.of("single roleType", "{\"actorId\":[\"2001\"],\"roleType\":[\"ORGANISATION\"]}",
                         "638e8e7a-7d7c-4027-9d53-200000000001"),

            Arguments.of("multiple roleType", "{\"actorId\":[\"2001\",\"2002\"]," +
                             "\"roleType\":[\"ORGANISATION\",\"CASE\"]}",
                         "638e8e7a-7d7c-4027-9d53-200000000001,638e8e7a-7d7c-4027-9d53-200000000002," +
                             "638e8e7a-7d7c-4027-9d53-200000000003"),

            Arguments.of("single roleName", "{\"actorId\":[\"3001\"],\"roleName\":[\"judge\"]}",
                         "638e8e7a-7d7c-4027-9d53-300000000001"),

            Arguments.of("multiple roleName", "{\"actorId\":[\"3001\",\"3002\"],\"roleName\":[\"judge\"," +
                             "\"case-allocator\"]}",
                         "638e8e7a-7d7c-4027-9d53-300000000001,638e8e7a-7d7c-4027-9d53-300000000002," +
                             "638e8e7a-7d7c-4027-9d53-300000000003"),

            Arguments.of("single classification", "{\"actorId\":[\"4001\"],\"classification\":[\"PUBLIC\"]}",
                         "638e8e7a-7d7c-4027-9d53-400000000001"),

            Arguments.of("multiple classification", "{\"actorId\":[\"4001\",\"4002\"]," +
                             "\"classification\":[\"PUBLIC\",\"PRIVATE\"]}",
                         "638e8e7a-7d7c-4027-9d53-400000000001,638e8e7a-7d7c-4027-9d53-400000000002," +
                             "638e8e7a-7d7c-4027-9d53-400000000003"),

            Arguments.of("single grantType", "{\"actorId\":[\"5001\"],\"grantType\":[\"STANDARD\"]}",
                         "638e8e7a-7d7c-4027-9d53-500000000001"),

            Arguments.of("multiple grantType", "{\"actorId\":[\"5001\",\"5002\"]," +
                             "\"grantType\":[\"STANDARD\",\"SPECIFIC\"]}",
                         "638e8e7a-7d7c-4027-9d53-500000000001,638e8e7a-7d7c-4027-9d53-500000000002," +
                             "638e8e7a-7d7c-4027-9d53-500000000003"),

            Arguments.of("single roleCategory", "{\"actorId\":[\"6001\"],\"roleCategory\":[\"JUDICIAL\"]}",
                         "638e8e7a-7d7c-4027-9d53-600000000001"),

            Arguments.of("multiple roleCategory", "{\"actorId\":[\"6001\",\"6002\"]," +
                             "\"roleCategory\":[\"JUDICIAL\",\"LEGAL_OPERATIONS\"]}",
                         "638e8e7a-7d7c-4027-9d53-600000000001,638e8e7a-7d7c-4027-9d53-600000000002," +
                             "638e8e7a-7d7c-4027-9d53-600000000003"),

            Arguments.of("ValidAt before begin date",
                         json("{\"actorId\":[\"7001\"],\"validAt\":\"<today -20>T00:00\"}"),
                         "638e8e7a-7d7c-4027-9d53-700000000001,638e8e7a-7d7c-4027-9d53-700000000003"),

            Arguments.of("ValidAt between begin & end dates",
                         json("{\"actorId\":[\"7001\"],\"validAt\":\"<today +2>T00:00\"}"),
                         "638e8e7a-7d7c-4027-9d53-700000000001,638e8e7a-7d7c-4027-9d53-700000000002," +
                             "638e8e7a-7d7c-4027-9d53-700000000003,638e8e7a-7d7c-4027-9d53-700000000004"),

            Arguments.of("ValidAt after end date",
                         json("{\"actorId\":[\"7001\"],\"validAt\":\"<today +20>T00:00\"}"),
                         "638e8e7a-7d7c-4027-9d53-700000000001,638e8e7a-7d7c-4027-9d53-700000000002"),

            Arguments.of("single attributes", "{\"actorId\":[\"8001\"]," +
                             "\"attributes\":[\"{\\\"region\\\": \\\"north-east\\\", " +
                             "\\\"contractType\\\": \\\"SALARIED\\\", \\\"jurisdiction\\\": \\\"divorce\\\"}\"]}",
                         "638e8e7a-7d7c-4027-9d53-800000000001"),

            Arguments.of("multiple attributes", "{\"actorId\":[\"8001\",\"8002\"]," +
                             "\"attributes\":[\"{\\\"region\\\": \\\"north-east\\\"," +
                             " \\\"contractType\\\": \\\"SALARIED\\\"}\"]}",
                         "638e8e7a-7d7c-4027-9d53-800000000001,638e8e7a-7d7c-4027-9d53-800000000002," +
                             "638e8e7a-7d7c-4027-9d53-800000000003")

        );
    }
    private static Stream<Arguments> queryProviderV2() {
        return Stream.of(
            Arguments.of("single actorId V2", "{\"queryRequests\":[{\"actorId\":[\"1001\"]}]}",
                         "638e8e7a-7d7c-4027-9d53-100000000001"),

            Arguments.of("multiple actorId V2", "{\"queryRequests\":[{\"actorId\":[\"1001\",\"1002\"]}]}",
                         "638e8e7a-7d7c-4027-9d53-100000000001,638e8e7a-7d7c-4027-9d53-100000000002," +
                             "638e8e7a-7d7c-4027-9d53-100000000003"),

            Arguments.of("single roleType V2", "{\"queryRequests\":[{\"actorId\":[\"2001\"],\"roleType\":[\"ORGANISATION\"]}]}",
                         "638e8e7a-7d7c-4027-9d53-200000000001"),

            Arguments.of("multiple roleType V2", "{\"queryRequests\":[{\"actorId\":[\"2001\",\"2002\"]," +
                             "\"roleType\":[\"ORGANISATION\",\"CASE\"]}]}",
                         "638e8e7a-7d7c-4027-9d53-200000000001,638e8e7a-7d7c-4027-9d53-200000000002," +
                             "638e8e7a-7d7c-4027-9d53-200000000003"),

            Arguments.of("ValidAt before begin date V2",
                         json("{\"queryRequests\":[{\"actorId\":[\"7001\"],\"validAt\":\"<today -20>T00:00\"}]}"),
                         "638e8e7a-7d7c-4027-9d53-700000000001,638e8e7a-7d7c-4027-9d53-700000000003"),

            Arguments.of("ValidAt between begin & end dates V2",
                         json("{\"queryRequests\":[{\"actorId\":[\"7001\"],\"validAt\":\"<today +2>T00:00\"}]}"),
                         "638e8e7a-7d7c-4027-9d53-700000000001,638e8e7a-7d7c-4027-9d53-700000000002," +
                             "638e8e7a-7d7c-4027-9d53-700000000003,638e8e7a-7d7c-4027-9d53-700000000004"),

            Arguments.of("ValidAt after end date V2",
                         json("{\"queryRequests\":[{\"actorId\":[\"7001\"],\"validAt\":\"<today +20>T00:00\"}]}"),
                         "638e8e7a-7d7c-4027-9d53-700000000001,638e8e7a-7d7c-4027-9d53-700000000002")
        );
    }

    private static String json(String template) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;

        Pattern pattern = Pattern.compile("<today ([+-]\\d+)>T00:00");
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            int offset = Integer.parseInt(matcher.group(1));
            String date = today.plusDays(offset).format(formatter) + "T00:00:00Z";
            matcher.appendReplacement(result, date);
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
