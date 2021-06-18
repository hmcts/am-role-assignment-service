package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.roleassignment.BaseTest;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.QueryRequests;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.versions.V2;

import javax.inject.Inject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {"launchdarkly.sdk.environment=pr"})
public class QueryAssignmentIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentIntegrationTest.class);

    final String URL = "/am/role-assignments/query";

    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445613";

    private MockMvc mockMvc;

    @Inject
    private WebApplicationContext wac;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldGetIdLdDemo() throws Exception {

        logger.info("Launch Darkly flag check is successful for the endpoint");
        final String url = "/am/role-assignments/ld/endpoint";

        final MvcResult result = mockMvc.perform(get(url).contentType(JSON_CONTENT_TYPE))
            .andExpect(status().isOk())
            .andReturn();
        String responseAsString = result.getResponse().getContentAsString();
        assertEquals("Launch Darkly flag check is successful for the endpoint", responseAsString);
    }

    @Test
    public void retrieveRoleAssignmentsByQueryRequest_withoutBody() throws Exception {

        logger.info("Retrieve Role Assignments without Body");

        mockMvc.perform(post(URL)
             .contentType(JSON_CONTENT_TYPE))
             .andExpect(status().is(400))
             .andExpect(jsonPath("$.errorDescription")
                           .value("Input for Request body parameter is not valid"))
             .andReturn();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void retrieveRoleAssignmentsByQueryRequest_emptyQueryRequestAndPageSize() throws Exception {

        logger.info("Retrieve Role Assignments with empty Query Request to verify return all entries with size 2");

        final MvcResult result = mockMvc.perform(post(URL)
                                                 .contentType(JSON_CONTENT_TYPE)
                                                 .headers(getHttpHeaders("2"))
                                                 .content(mapper.writeValueAsBytes(QueryRequest.builder().build())))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode responseJsonNode = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                                                           JsonNode.class);
        assertFalse(responseJsonNode.get("roleAssignmentResponse").isEmpty());
        assertEquals(2, responseJsonNode.get("roleAssignmentResponse").size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void retrieveRoleAssignmentsByQueryRequest_queryRequestWithoutHeaders() throws Exception {

        logger.info("Retrieve Role Assignments with Query Request");

        QueryRequest queryRequest = createQueryRequest();
        mockMvc.perform(post(URL)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .content(mapper.writeValueAsBytes(queryRequest)))
                                                     .andExpect(status().isOk())
            .andExpect(jsonPath("$.roleAssignmentResponse[0]").exists())
            .andExpect(jsonPath("$.roleAssignmentResponse[0].roleType")
                           .value(queryRequest.getRoleType().get(0)))
            .andExpect(jsonPath("$.roleAssignmentResponse[0].roleName")
                           .value(queryRequest.getRoleName().get(0)))
            .andExpect(jsonPath("$.roleAssignmentResponse[0].grantType")
                           .value(queryRequest.getGrantType().get(0)))
            .andExpect(jsonPath("$.roleAssignmentResponse[0].actorId")
                           .value(queryRequest.getActorId().get(0)))
            .andReturn();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void retrieveRoleAssignmentsByQueryRequest_unmatchedQueryRequest() throws Exception {

        logger.info("Retrieve Role Assignments with unmatched Query Request");

        QueryRequest queryRequest = QueryRequest.builder()
            .actorId(List.of(ACTOR_ID))
            .roleCategory(List.of(RoleCategory.JUDICIAL.toString()))
            .validAt(now())
            .build();
        mockMvc.perform(post(URL)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .content(mapper.writeValueAsBytes(queryRequest)))
                                                     .andExpect(status().isOk())
            .andExpect(jsonPath("$.roleAssignmentResponse").isEmpty())
            .andReturn();
    }

    @Test
    public void retrieveRoleAssignmentsByQueryRequestV2_withoutBody() throws Exception {

        logger.info("Retrieve Role Assignments without Body");

        mockMvc.perform(post(URL)
                            .contentType(V2.MediaType.POST_ASSIGNMENTS))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().is(400))
            .andExpect(jsonPath("$.errorDescription")
                           .value("Input for Request body parameter is not valid"))
            .andReturn();
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void retrieveRoleAssignmentsByQueryRequestV2_queryRequests() throws Exception {

        logger.info("Retrieve Role Assignments with two Query Requests in the list to verify return all entries");
        QueryRequest queryRequest = createQueryRequest();
        QueryRequest queryRequest2 = QueryRequest.builder()
            .actorId(List.of(ACTOR_ID))
            .roleType(List.of(RoleType.CASE.toString())).build();
        QueryRequests queryRequests  =  QueryRequests.builder().queryRequests(
            List.of(queryRequest, queryRequest2)).build();

        final MvcResult result = mockMvc.perform(post("/am/role-assignments/query")
                                                     .contentType(V2.MediaType.POST_ASSIGNMENTS)
                                                     .headers(getHttpHeaders("20"))
                                                     .content(mapper.writeValueAsString(queryRequests)))
            .andExpect(status().isOk())
            //.andDo(print())
            .andExpect(jsonPath("$.roleAssignmentResponse[0]").exists())
            .andExpect(jsonPath("$.roleAssignmentResponse[0].roleName")
                           .value(queryRequest.getRoleName().get(0)))
            .andExpect(jsonPath("$.roleAssignmentResponse[0].actorId")
                           .value(queryRequest.getActorId().get(0)))
            .andExpect(jsonPath("$.roleAssignmentResponse[0].grantType")
                           .value(queryRequest.getGrantType().get(0)))
            .andExpect(jsonPath("$.roleAssignmentResponse[0].roleType")
                           .value(queryRequest.getRoleType().get(0)))
            .andReturn();
        JsonNode responseJsonNode = new ObjectMapper().readValue(result.getResponse().getContentAsString(),JsonNode.class);
        assertFalse(responseJsonNode.get("roleAssignmentResponse").isEmpty());
        assertEquals(3, responseJsonNode.get("roleAssignmentResponse").size());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void retrieveRoleAssignmentsByQueryRequestV2_emptyQueryRequestsAndPageSize() throws Exception {

        logger.info("Retrieve Role Assignments with empty Query Request to verify return all entries with 2 size");
        QueryRequest queryRequest = QueryRequest.builder().build();
        QueryRequests queryRequests  =  QueryRequests.builder().queryRequests(List.of(queryRequest)).build();

        final MvcResult result = mockMvc.perform(post("/am/role-assignments/query")
                                                     .contentType(V2.MediaType.POST_ASSIGNMENTS)
                                                     .headers(getHttpHeaders("2"))
                                                     .content(mapper.writeValueAsString(queryRequests))
                                                     .accept(V2.MediaType.POST_ASSIGNMENTS))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode responseJsonNode = new ObjectMapper().readValue(result.getResponse().getContentAsString(),JsonNode.class);
        assertFalse(responseJsonNode.get("roleAssignmentResponse").isEmpty());
        assertEquals(2, responseJsonNode.get("roleAssignmentResponse").size());
    }

    public static QueryRequest createQueryRequest() {
        Map<String, List<String>> attributes = new HashMap<>();
        List<String> regions = Arrays.asList("London", "JAPAN", "north-east");
        List<String> contractTypes = Arrays.asList("SALARIED", "Non SALARIED");
        attributes.put("region", regions);
        attributes.put("contractType", contractTypes);

        return QueryRequest.builder()
            .actorId(List.of(ACTOR_ID))
            .roleType(List.of(RoleType.ORGANISATION.toString()))
            .roleName(List.of("judge"))
            .classification(List.of(Classification.PUBLIC.toString()))
            .grantType(List.of(GrantType.STANDARD.toString()))
            .validAt(now())
            .attributes(attributes)
            .build();
    }

    @NotNull
    private HttpHeaders getHttpHeaders(String size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("pageNumber", "0");
        headers.add("size", size);
        headers.add("sort", "id");
        headers.add("direction", "asc");
        headers.add(Constants.CORRELATION_ID_HEADER_NAME, "38a90097-434e-47ee-8ea1-9ea2a267f51d");

        return headers;
    }
}
