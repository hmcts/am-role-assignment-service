package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.TokenRequest;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.GetAssignmentController;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.QueryAssignmentController;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RoleAssignmentIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentIntegrationTest.class);
    private static final String COUNT_HISTORY_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment_history";
    private static final String COUNT_ASSIGNMENT_RECORDS_QUERY = "SELECT count(1) as n FROM role_assignment";

    private static final String GET_ASSIGNMENT_STATUS_QUERY = "SELECT actor_id FROM role_assignment where id = ?";
    private transient MockMvc mockMvc;

    private JdbcTemplate template;

    RestTemplate restTemplate = new RestTemplate();

    @Inject
    private WebApplicationContext wac;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GetAssignmentController getAssignmentController;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private PersistenceService persistenceService = mock(PersistenceService.class);

    @Mock
    private PrepareResponseService prepareResponseService = mock(PrepareResponseService.class);

    @Mock
    private SecurityUtils securityUtils;

    @Inject
    protected DataSource db;

    private final QueryRoleAssignmentOrchestrator queryRoleAssignmentOrchestrator =
        mock(QueryRoleAssignmentOrchestrator.class);

    @InjectMocks
    private final QueryAssignmentController sut = new QueryAssignmentController(queryRoleAssignmentOrchestrator);

    RetrieveRoleAssignmentOrchestrator retrieveRoleAssignmentOrchestrator = new RetrieveRoleAssignmentOrchestrator(
        persistenceService,
        prepareResponseService
    );

    @Before
    public void setUp() {
        template = new JdbcTemplate(db);
        //mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        //mockMvc = MockMvcBuilders.standaloneSetup(sut).build();
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.initMocks(this);

        doReturn(authentication).when(securityContext).getAuthentication();

        SecurityContextHolder.setContext(securityContext);
        getAssignmentController = new GetAssignmentController(retrieveRoleAssignmentOrchestrator);

        //MockUtils.setSecurityAuthorities(UID_NO_EVENT_ACCESS, authentication, MockUtils.ROLE_CASEWORKER_PUBLIC);

    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {
        "classpath:sql/insert_role_assignment_request.sql",
        "classpath:sql/insert_role_assignment_history.sql"
    })
    public void shoudGetRecordCountFromHistoryTable() throws Exception {
        final int count = template.queryForObject(COUNT_HISTORY_RECORDS_QUERY, Integer.class);
        logger.info(" Total number of records fetched from role assignment history table...{}", count);
        assertEquals(
            "role_assignment_history record count ", 15, count);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shoudGetRecordsFromRoleAssignmentTable() throws Exception {
        final Object[] parameters = new Object[]{
            "638e8e7a-7d7c-4027-9d53-ea4b1095eab1"
        };
        String actorId = template.queryForObject(GET_ASSIGNMENT_STATUS_QUERY, parameters, String.class);
        logger.info(" Role assignment actor id is...{}", actorId);
        assertEquals(
            "Role assignment actor Id", "123e4567-e89b-42d3-a456-556642445613", actorId);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
        {"classpath:sql/insert_role_assignment.sql",
            "classpath:sql/insert_actor_cache_control.sql"})
    public void shouldGetRoleAssignmentsBasedOnActorId() throws Exception {
        assertRoleAssignmentRecordSize();
        final String url = "/am/role-assignments/actors/123e4567-e89b-42d3-a456-556642445612";

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(JSON_CONTENT_TYPE)
                                                     .headers(getHttpHeaders()))
            .andExpect(status().is(200))
            .andReturn();

        logger.info(" result...{}", result);
        RoleAssignmentResource response = mapper.readValue(
            result.getResponse().getContentAsString(),
            RoleAssignmentResource.class
        );
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(
            "2ef8ebf3-266e-45d3-a3b8-4ce1e5d93b9f",
            response.getRoleAssignmentResponse().get(0).getId().toString()
        );
        assertEquals(
            "123e4567-e89b-42d3-a456-556642445612",
            response.getRoleAssignmentResponse().get(0).getActorId().toString()
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_role_assignment.sql"})
    public void shouldGetRoleAssignmentsBasedOnRoleTypeAndActorId() throws Exception {
        assertRoleAssignmentRecordSize();
        final String url = "/am/role-assignments";

        final MvcResult result = mockMvc.perform(get(url)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(getHttpHeaders())
        )
            .andExpect(status().is(200))
            .andReturn();
        String responseAsString = result.getResponse().getContentAsString();

        List<RoleAssignment> response = Arrays.asList(mapper.readValue(responseAsString, RoleAssignment[].class));
        String contentAsString = result.getResponse().getContentAsString();

    }

    @NotNull
    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    private void assertRoleAssignmentRecordSize() {
        final Object[] parameters = new Object[]{
            "2ef8ebf3-266e-45d3-a3b8-4ce1e5d93b9f"
        };
        String actorId = template.queryForObject(GET_ASSIGNMENT_STATUS_QUERY, parameters, String.class);
        logger.info(" Role assignment actor id is...{}", actorId);
        assertEquals(
            "Role assignment actor Id", "123e4567-e89b-42d3-a456-556642445612", actorId);
    }

    /*@Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/sample.sql" })
    public void shouldDeleteRoleAssignments() throws Exception {
        //doReturn("serviceName").when(securityUtils).getServiceName();
        //doReturn("638e8e7a-7d7c-4027-9d53-ea4b1095eab1").when(securityUtils).getUserId();
        final String url = "/am/role-assignments";
        String userToken = searchUserByUserId();
        logger.info(userToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTYyMjIwNDZ9.6dyTWiXaaSjfOZpqg9ieHakQARgG5rdlJjfoolI70RJD4V0gRFMFIJ6ULF3c-SBoQAZk0rhlixf7Id74DA7iBQ");
        headers.add("Authorization", userToken);

        final MvcResult result = mockMvc.perform(delete(url)
                                                     .contentType(MediaType.APPLICATION_JSON)
                                                     .headers(headers)
                                                     .param("process", "S-052")
                                                     .param("reference", "S-052")
        )
            .andExpect(status().is(200))
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        logger.info(" contentAsString...{}", contentAsString);

    }


    public String searchUserByUserId() {

        ResponseEntity<TokenResponse> response = new ResponseEntity<>(HttpStatus.OK);
        HttpHeaders headers = new HttpHeaders();
        try {
            String url = String.format(
                "%s/o/token?client_id=%s&client_secret=%s&grant_type=%s&scope=%s&username=%s&password=%s",
                "http://localhost:5000",
                "am_docker",
                "am_docker_secret",
                "password",
                "openid+profile+roles+authorities",
                "befta.caseworker.2.solicitor.2@gmail.com",
                "Pa55word11"
            );
            logger.info("URL :   {}", url);
            headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
            HttpEntity<?> entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                TokenResponse.class
            );

            if (HttpStatus.OK.equals(response.getStatusCode())) {
                logger.info("Positive response");
                return response.getBody().accessToken;
            } else {
                logger.error("There is some problem in fetching access token {}", response
                    .getStatusCode());
                throw new ResourceNotFoundException("Not Found");
            }
        } catch (HttpClientErrorException exception) {
            logger.error("HttpClientErrorException {}", exception.getMessage());
            throw new BadRequestException("Unable to fetch access token");

        }
    }*/
}
