package uk.gov.hmcts.reform.roleassignment.controller;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
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
import uk.gov.hmcts.reform.roleassignment.versions.V2;

@TestPropertySource(properties = {"ras.environment=pr"})
class RoleAssignmentIntegrationQueryTest extends BaseTest {

    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
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

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
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
                                                     .headers(getHttpHeaders())
                                                     .content(queryJson.getBytes())
        ).andExpect(status().is(200)).andReturn();

        List<ExistingRoleAssignment> existingRoleAssignments = getExistingRoleAssignmentFromMvcResult(result);

        assertNotNull(existingRoleAssignments);
        assertEquals(expectedMultipleRoleIds.size(), existingRoleAssignments.size());
        existingRoleAssignments.forEach(element ->
                                            assertTrue(expectedMultipleRoleIds.contains(element.getId().toString()))
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
                           "actorId":["1001","1002"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-100000000001",
                                 "638e8e7a-7d7c-4027-9d53-100000000002",
                                 "638e8e7a-7d7c-4027-9d53-100000000003")),

            // RoleType Tests CASE, ORGANISATION
            Arguments.of("single roleType", """
                         {
                           "actorId":["2001"],
                           "roleType":["ORGANISATION"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-200000000001")),

            Arguments.of("multiple roleType", """
                         {
                           "actorId":["2001","2002"],
                           "roleType":["ORGANISATION","CASE"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-200000000001",
                                 "638e8e7a-7d7c-4027-9d53-200000000002",
                                 "638e8e7a-7d7c-4027-9d53-200000000003")),

            // RoleName Tests judge, case-allocator, hearing-manager, hearing-viewer
            Arguments.of("single roleName", """
                         {
                           "actorId":["3001"],
                           "roleName":["judge"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-300000000001")),

            Arguments.of("multiple roleName", """
                         {
                           "actorId":["3001","3002"],
                           "roleName":["judge",
                           "case-allocator"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-300000000001",
                                 "638e8e7a-7d7c-4027-9d53-300000000002",
                                 "638e8e7a-7d7c-4027-9d53-300000000003"),

            // Classification Test PUBLIC, PRIVATE, RESTRICTED
            Arguments.of("single classification", """
                         {
                           "actorId":["4001"],
                           "classification":["PUBLIC"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-400000000001")),

            Arguments.of("multiple classification", """
                         {
                           "actorId":["4001","4002"],
                           "classification":["PUBLIC","PRIVATE"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-400000000001",
                                 "638e8e7a-7d7c-4027-9d53-400000000002",
                                 "638e8e7a-7d7c-4027-9d53-400000000003")),

            // grantType Tests BASIC, SPECIFIC, STANDARD, CHALLENGED, EXCLUDED
            Arguments.of("single grantType", """
                         {
                           "actorId":["5001"],
                           "grantType":["STANDARD"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-500000000001")),

            Arguments.of("multiple grantType", """
                         {
                           "actorId":["5001","5002"],
                            "grantType":["STANDARD","SPECIFIC"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-500000000001",
                                 "638e8e7a-7d7c-4027-9d53-500000000002",
                                 "638e8e7a-7d7c-4027-9d53-500000000003")),

            // RoleCategory Tests JUDICIAL, LEGAL_OPERATIONS, ADMIN, PROFESSIONAL,
            //                    CITIZEN, SYSTEM, OTHER_GOV_DEPT, CTSC
            Arguments.of("single roleCategory", """
                         {
                           "actorId":["6001"],
                           "roleCategory":["JUDICIAL"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-600000000001")),

            Arguments.of("multiple roleCategory", """
                         {
                           "actorId":["6001","6002"],
                           "roleCategory":["JUDICIAL","LEGAL_OPERATIONS"]
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-600000000001",
                                 "638e8e7a-7d7c-4027-9d53-600000000002",
                                 "638e8e7a-7d7c-4027-9d53-600000000003")),

            // ValidAt Tests
            Arguments.of("ValidAt before begin date",
                         json("""
                         {
                           "actorId":["7001"],
                           "validAt":"<today -20>T00:00"
                         }
                         """),
                         List.of("638e8e7a-7d7c-4027-9d53-700000000001",
                                 "638e8e7a-7d7c-4027-9d53-700000000003")),

            Arguments.of("ValidAt between begin & end dates",
                         json("""
                         {
                           "actorId":["7001"],
                           "validAt":"<today +2>T00:00"
                         }
                         """),
                         List.of("638e8e7a-7d7c-4027-9d53-700000000001",
                                 "638e8e7a-7d7c-4027-9d53-700000000002",
                                 "638e8e7a-7d7c-4027-9d53-700000000003",
                                 "638e8e7a-7d7c-4027-9d53-700000000004")),

            Arguments.of("ValidAt after end date",
                         json("""
                         {
                           "actorId":["7001"],
                           "validAt":"<today +20>T00:00"
                         }
                         """),
                         List.of("638e8e7a-7d7c-4027-9d53-700000000001",
                                 "638e8e7a-7d7c-4027-9d53-700000000002")),

            // Attributes Tests
            Arguments.of("single attributes", """
                         {
                           "actorId": ["8001"],
                           "attributes": { "region": ["north-east"], "jurisdiction": ["divorce"] }
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-800000000001")),

            Arguments.of("multiple attributes", """
                         {
                           "actorId": ["8001", "8002"],
                           "attributes": { "region": ["north-east"], "contractType": ["SALARIED"] }
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-800000000001",
                                 "638e8e7a-7d7c-4027-9d53-800000000002",
                                 "638e8e7a-7d7c-4027-9d53-800000000003")))
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
            Arguments.of("single readOnly",
                         """
                         {
                          "readOnly": true
                         }
                         """,
                         List.of("638e8e7a-7d7c-4027-9d53-010000000001"))
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
