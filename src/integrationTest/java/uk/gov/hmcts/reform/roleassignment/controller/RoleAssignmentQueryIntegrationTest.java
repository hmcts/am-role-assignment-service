package uk.gov.hmcts.reform.roleassignment.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.BaseTest;
import uk.gov.hmcts.reform.roleassignment.MockUtils;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.versions.V2;

@TestPropertySource(properties = {"ras.environment=pr"})
class RoleAssignmentQueryIntegrationTest extends BaseTest {

    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    private static final String URL_QUERY_ROLE_ASSIGNMENTS = "/am/role-assignments/query";
    private static final String URL_DELETE_ROLE_ASSIGNMENTS = "/am/role-assignments/query/delete";

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Inject
    private WebApplicationContext wac;

    @MockitoBean
    private IdamApi idamApi;

    @Autowired
    private DataSource ds;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

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

    private List<RoleAssignmentEntity> getAllRoleAssignmentFromDb() {
        return roleAssignmentRepository.findAll();
    }

    @NotNull
    private HttpHeaders getHttpHeaders(String serviceName) {
        HttpHeaders headers = new HttpHeaders();
        var s2SToken = MockUtils.generateDummyS2SToken(serviceName);
        headers.add("ServiceAuthorization", "Bearer " + s2SToken);
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    @ParameterizedTest(name = "queryRequestsV1 {0}")
    @MethodSource("queryProvider")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_role_assignment_querytest.sql"})
    void queryRequestsV1Test(String testName, String queryJson, List<String> expectedRoleIds) throws Exception {

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders("civil_service"))
                                                     .content(queryJson.getBytes())
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> remainingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(remainingRoleAssignments);
        assertEquals(expectedRoleIds.size(), remainingRoleAssignments.size());
        remainingRoleAssignments.forEach(element ->
                                            assertTrue(expectedRoleIds.contains(element.getId().toString()))
        );
    }

    @ParameterizedTest(name = "queryRequestsV2 {0}")
    @MethodSource("queryProviderV2")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_role_assignment_querytest.sql"})
    void queryRequestsV2Test(String testName, String queryJson, List<String> expectedRoleIds) throws Exception {
        queryJson = """
            {
              "queryRequests":[
                """ + queryJson + """
              ]
            } """; // Wrap in queryRequests for V2

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(V2.MediaType.POST_ASSIGNMENTS)
                                                     .headers(getHttpHeaders("civil_service"))
                                                     .content(queryJson.getBytes())
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> remainingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(remainingRoleAssignments);
        assertEquals(expectedRoleIds.size(), remainingRoleAssignments.size());
        remainingRoleAssignments.forEach(element ->
                                            assertTrue(expectedRoleIds.contains(element.getId().toString()))
        );
    }

    @ParameterizedTest(name = "multipleQueryRequestsV2 {0}")
    @MethodSource("queryProviderV2")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_role_assignment_querytest.sql"})
    void multipleQueryRequestsV2Test(String testName, String queryJson, List<String> expectedRoleIds) throws Exception {
        List<String> expectedMultipleRoleIds = new ArrayList<>();
        expectedMultipleRoleIds.addAll(expectedRoleIds);
        expectedMultipleRoleIds.add("638e8e7a-7d7c-4027-9d53-ea4b1095eab1");
        queryJson = """
            {
              "queryRequests":[
                {
                  "actorId": ["123e4567-e89b-42d3-a456-556642445613"]
                },
                """ + queryJson + """
              ]
            } """; // Wrap in queryRequests for V2

        final MvcResult result = mockMvc.perform(post(URL_QUERY_ROLE_ASSIGNMENTS)
                                                     .contentType(V2.MediaType.POST_ASSIGNMENTS)
                                                     .headers(getHttpHeaders("civil_service"))
                                                     .content(queryJson.getBytes())
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> remainingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(remainingRoleAssignments);
        assertEquals(expectedMultipleRoleIds.size(), remainingRoleAssignments.size());
        remainingRoleAssignments.forEach(element ->
                                            assertTrue(expectedMultipleRoleIds.contains(element.getId().toString()))
        );
    }

    @ParameterizedTest(name = "deleteRequestsV2 {0}")
    @MethodSource("queryProviderV2")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_role_assignment_querytest.sql"})
    void deleteRequestsV2Test(String testName, String queryJson, List<String> expectedRoleIds) throws Exception {
        queryJson = """
            {
              "queryRequests":[
                """ + queryJson + """
              ]
            } """; // Wrap in queryRequests for V2

        List<RoleAssignmentEntity> previousRoleAssignmentsOnDb = getAllRoleAssignmentFromDb();

        // Each deleted record must have the attribute of "jurisdiction": "WA"
        // and use the header client_id=wa_workflow_api.  This combined with the
        // FeatureFlagEnum.WA_BYPASS_1_0, being set in test, bypasses the drool
        // rules for authorisation and allows the deletion.
        final MvcResult result = mockMvc.perform(post(URL_DELETE_ROLE_ASSIGNMENTS)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders("wa_workflow_api"))
                                                     .content(queryJson.getBytes())
        ).andExpect(status().is(200)).andReturn();

        List<RoleAssignmentEntity> roleAssignmentsOnDb = getAllRoleAssignmentFromDb();

        assertNotNull(roleAssignmentsOnDb);
        assertEquals(previousRoleAssignmentsOnDb.size() - expectedRoleIds.size(),
                     roleAssignmentsOnDb.size());
        // confirm expectedRoleIds no longer present
        roleAssignmentsOnDb.forEach(element ->
                                            assertFalse(expectedRoleIds.contains(element.getId().toString()))
        );
    }

    @ParameterizedTest(name = "multipleDeleteRequestsV2 {0}")
    @MethodSource("queryProviderV2")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"classpath:sql/insert_role_assignment_querytest.sql"})
    void multipleDeleteRequestsV2Test(String testName, String queryJson, List<String> expectedRoleIds)
        throws Exception {
        List<String> expectedMultipleRoleIds = new ArrayList<>();
        expectedMultipleRoleIds.addAll(expectedRoleIds);
        expectedMultipleRoleIds.add("638e8e7a-7d7c-4027-9d53-ea4b1095eab1");
        queryJson = """
            {
              "queryRequests":[
                {
                  "actorId": ["123e4567-e89b-42d3-a456-556642445613"]
                },
                """ + queryJson + """
              ]
            } """; // Wrap in queryRequests for V2

        List<RoleAssignmentEntity> previousRoleAssignmentsOnDb = getAllRoleAssignmentFromDb();

        final MvcResult result = mockMvc.perform(post(URL_DELETE_ROLE_ASSIGNMENTS)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders("wa_workflow_api"))
                                                     .content(queryJson.getBytes())
        ).andExpect(status().is(200)).andReturn();

        List<RoleAssignmentEntity> roleAssignmentsOnDb = getAllRoleAssignmentFromDb();

        assertNotNull(roleAssignmentsOnDb);
        assertEquals(previousRoleAssignmentsOnDb.size() - expectedMultipleRoleIds.size(),
                     roleAssignmentsOnDb.size());
        // confirm expectedRoleIds no longer present
        roleAssignmentsOnDb.forEach(element ->
                                            assertFalse(expectedRoleIds.contains(element.getId().toString()))
        );
    }

    private static Stream<Arguments> queryProvider() {
        return Stream.of(
            // ActorID test
            Arguments.of("single actorId", """
                         {
                           "actorId":["1001"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-100000000001")),

            Arguments.of("multiple actorId", """
                         {
                           "actorId":["1001","1002","1003"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-100000000001",
                                 "638e8e7a-7d7c-4027-9d53-100000000002",
                                 "638e8e7a-7d7c-4027-9d53-100000000003")),

            // RoleType Tests CASE, ORGANISATION
            Arguments.of("single roleType", """
                         {
                           "roleType":["ORGANISATION"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-200000000001")),

            Arguments.of("multiple roleType", """
                         {
                           "roleType":["ORGANISATION","CASE"],
                           "attributes": { "roleType": ["Test"] }
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-200000000001",
                                 "638e8e7a-7d7c-4027-9d53-200000000002",
                                 "638e8e7a-7d7c-4027-9d53-200000000003")),

            // RoleName Tests solicitor, case-allocator, hearing-manager, hearing-viewer
            Arguments.of("single roleName", """
                         {
                           "roleName":["Solicitor"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-300000000001")),

            Arguments.of("multiple roleName", """
                         {
                           "roleName":["Solicitor",
                           "hearing-manager"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-300000000001",
                                 "638e8e7a-7d7c-4027-9d53-300000000002",
                                 "638e8e7a-7d7c-4027-9d53-300000000003")),

            // Classification Test PUBLIC, PRIVATE, RESTRICTED
            Arguments.of("single classification", """
                         {
                           "classification":["RESTRICTED"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-400000000001")),

            Arguments.of("multiple classification", """
                         {
                           "classification":["RESTRICTED","PRIVATE"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-400000000001",
                                 "638e8e7a-7d7c-4027-9d53-400000000002",
                                 "638e8e7a-7d7c-4027-9d53-400000000003")),

            // grantType Tests BASIC, SPECIFIC, STANDARD, CHALLENGED, EXCLUDED
            Arguments.of("single grantType", """
                         {
                           "grantType":["CHALLENGED"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-500000000001")),

            Arguments.of("multiple grantType", """
                         {
                            "grantType":["CHALLENGED","EXCLUDED"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-500000000001",
                                 "638e8e7a-7d7c-4027-9d53-500000000002",
                                 "638e8e7a-7d7c-4027-9d53-500000000003")),

            // RoleCategory Tests JUDICIAL, LEGAL_OPERATIONS, ADMIN, PROFESSIONAL,
            //                    CITIZEN, SYSTEM, OTHER_GOV_DEPT, CTSC
            Arguments.of("single roleCategory", """
                         {
                           "roleCategory":["JUDICIAL"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-600000000001")),

            Arguments.of("multiple roleCategory", """
                         {
                           "roleCategory":["JUDICIAL","OTHER_GOV_DEPT"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-600000000001",
                                 "638e8e7a-7d7c-4027-9d53-600000000002",
                                 "638e8e7a-7d7c-4027-9d53-600000000003")),

            // ValidAt Tests
            Arguments.of("ValidAt before begin date",
                         json("""
                         {
                           "validAt":"2020-01-01T00:00"
                         }
                         """),
                         List.of("638e8e7a-7d7c-4027-9d53-700000000001",
                                 "638e8e7a-7d7c-4027-9d53-700000000003")),

            Arguments.of("ValidAt between begin & end dates",
                         json("""
                         {
                           "validAt":"<today +9>T00:00"
                         }
                         """),
                         List.of("638e8e7a-7d7c-4027-9d53-700000000001",
                                 "638e8e7a-7d7c-4027-9d53-700000000002",
                                 "638e8e7a-7d7c-4027-9d53-700000000003",
                                 "638e8e7a-7d7c-4027-9d53-700000000004")),

            Arguments.of("ValidAt after end date",
                         json("""
                         {
                           "validAt":"<today +20>T00:00"
                         }
                         """),
                         List.of("638e8e7a-7d7c-4027-9d53-700000000001",
                                 "638e8e7a-7d7c-4027-9d53-700000000002")),

            // Attributes Tests
            Arguments.of("single attributes", """
                         {
                           "attributes": { "region": ["south-east"], "caseType": ["CT2"] }
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-800000000001")),

            Arguments.of("multiple attributes", """
                         {
                           "attributes": { "region": ["south-east", "south-west"], "contractType": ["SALARIED"] }
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-800000000001",
                                 "638e8e7a-7d7c-4027-9d53-800000000002",
                                 "638e8e7a-7d7c-4027-9d53-800000000003")),

            // Authorisations
            Arguments.of("single authorizations", """
                         {
                           "authorisations":["auth1"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-110000000001")),

            Arguments.of("mutliple authorizations", """
                         {
                           "authorisations": [
                             "auth1", "auth4"
                           ]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-110000000001",
                                 "638e8e7a-7d7c-4027-9d53-110000000002"))
            );
    }

    private static Stream<Arguments> queryProviderV2() {
        return Stream.concat(queryProvider(), Stream.of(

            // hasAttributes Tests
            Arguments.of("single hasAttributes",
                         """
                         {
                           "hasAttributes": [
                             "unique1"
                           ]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-900000000001")),
            Arguments.of("multiple hasAttributes",
                         """
                         {
                           "hasAttributes": [
                             "unique1", "unique2"
                           ]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-900000000001",
                                 "638e8e7a-7d7c-4027-9d53-900000000002")),


            // readOnly Tests
            Arguments.of("single readOnly true",
                         """
                         {
                          "readOnly": true
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-010000000001")),
            Arguments.of("single readOnly false",
                         """
                         {
                          "actorId":["0101"],
                          "readOnly": false
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-010000000002"))
        ));
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
