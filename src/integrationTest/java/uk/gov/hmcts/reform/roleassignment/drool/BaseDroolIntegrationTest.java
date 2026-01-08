package uk.gov.hmcts.reform.roleassignment.drool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.BaseTest;
import uk.gov.hmcts.reform.roleassignment.MockUtils;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequestResource;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;


@Slf4j
@SuppressWarnings("SameParameterValue")
@TestPropertySource(properties = {
    "spring.cache.type=none",
    "ras.environment=pr"
})
public abstract class BaseDroolIntegrationTest extends BaseTest {

    public static final String AUTHORISED_SERVICE = "ccd_gw";
    public static final String AUTHORISED_SERVICE_XUI = "xuiwebapp";

    public static final String CASE_ID = "1234567890123456";

    public static final String ROLE_CASE_ALLOCATOR = "case-allocator";
    public static final String ROLE_HEARING_MANAGER = "hearing-manager";
    public static final String ROLE_HEARING_VIEWER = "hearing-viewer";

    public static final String URL_CREATE_ROLES = "/am/role-assignments";
    public static final String URL_DELETE_ROLES = "/am/role-assignments";

    public static final String TEST_AUTH_USER_ID = "6b36bfc6-bb21-11ea-b3de-0242ac130006";

    protected MockMvc mockMvc;

    @Inject
    private WebApplicationContext wac;

    @MockBean
    private IdamApi idamApi;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Inject
    protected PersistenceService persistenceService;

    @MockBean
    private RetrieveDataService retrieveDataService;

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);

        // default UserInfo
        setUpUserInfo(TEST_AUTH_USER_ID);
    }

    public void setUpUserInfo(String uid) {
        UserInfo userInfo = UserInfo.builder()
            .uid(uid)
            .sub("emailId@a.com")
            .build();

        doReturn(userInfo).when(idamApi).retrieveUserInfo(anyString());
    }

    protected HttpHeaders getHttpHeaders(String serviceName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, "Bearer user1");
        var s2SToken = MockUtils.generateDummyS2SToken(serviceName);
        headers.add("ServiceAuthorization", "Bearer " + s2SToken);
        headers.add(Constants.CORRELATION_ID_HEADER_NAME, "38a90097-434e-47ee-8ea1-9ea2a267f51d");
        return headers;
    }

    protected void mockRetrieveDataServiceGetCaseById(String caseId,
                                                      String jurisdiction,
                                                      String caseTypeId)  {
        Case retrievedCase = Case.builder()
            .id(caseId)
            .jurisdiction(jurisdiction)
            .caseTypeId(caseTypeId)
            .securityClassification(Classification.PUBLIC)
            .build();

        doReturn(retrievedCase).when(retrieveDataService).getCaseById(anyString());
    }

    protected void assertCreateRoleAssignmentResponseStatus(Status status,
                                                            MvcResult result,
                                                            int roleAssignmentCount) throws Exception {
        assertNotNull(result.getResponse().getContentAsString());

        RoleAssignmentRequestResource response = mapper.readValue(
            result.getResponse().getContentAsString(),
            RoleAssignmentRequestResource.class
        );

        log.info("Create RoleAssignment Response: status={}, body: {}",
             result.getResponse().getStatus(),
             writeValueAsPrettyJson(response)
        );

        assertNotNull(response);
        assertEquals(status, response.getRoleAssignmentRequest().getRequest().getStatus());
        assertEquals(roleAssignmentCount, response.getRoleAssignmentRequest().getRequestedRoles().size());
    }

    protected List<RoleAssignment> assertRoleAssignmentsInDb(String actorId, int expectedCount) {
        List<RoleAssignment> roleAssignments = persistenceService.getAssignmentsByActor(actorId);
        assertNotNull(roleAssignments);
        assertEquals(expectedCount, roleAssignments.size());
        return roleAssignments;
    }

    protected void assertSystemRoleAssignmentDefaultValues(String actorId,
                                                           List<RoleAssignment> roleAssignments,
                                                           String roleName,
                                                           String jurisdiction) {
        assertEquals(1, roleAssignments.size());
        assertSystemRoleAssignmentDefaultValues(actorId, roleAssignments.get(0), roleName, jurisdiction);
    }

    protected void assertSystemRoleAssignmentDefaultValues(String actorId,
                                                           RoleAssignment roleAssignment,
                                                           String roleName,
                                                           String jurisdiction) {
        assertEquals(actorId, roleAssignment.getActorId());
        assertEquals(ActorIdType.IDAM, roleAssignment.getActorIdType());
        assertEquals(RoleType.ORGANISATION, roleAssignment.getRoleType());
        assertEquals(roleName, roleAssignment.getRoleName());
        assertEquals(Classification.PUBLIC, roleAssignment.getClassification());
        assertEquals(GrantType.STANDARD, roleAssignment.getGrantType());
        assertEquals(RoleCategory.SYSTEM, roleAssignment.getRoleCategory());
        assertFalse(CollectionUtils.isEmpty(roleAssignment.getAttributes()));
        assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
    }

    protected void assertHearingSystemRoleAssignment(String actorId,
                                                     RoleAssignment roleAssignment,
                                                     String roleName,
                                                     String jurisdiction,
                                                     String caseType) {
        assertSystemRoleAssignmentDefaultValues(actorId, roleAssignment, roleName, jurisdiction);
        assertEquals(caseType, roleAssignment.getAttributes().get("caseType").asText());
    }

    protected void assertHearingSystemRoleAssignments(String actorId,
                                                      List<RoleAssignment> roleAssignments,
                                                      String jurisdiction,
                                                      String caseType) {
        AtomicBoolean foundHearingManager = new AtomicBoolean(false);
        AtomicBoolean foundHearingViewer = new AtomicBoolean(false);

        roleAssignments.forEach(roleAssignment -> {
            if (ROLE_HEARING_MANAGER.equals(roleAssignment.getRoleName())) {
                assertHearingSystemRoleAssignment(
                    actorId, roleAssignment, ROLE_HEARING_MANAGER, jurisdiction, caseType
                );
                foundHearingManager.set(true);
            } else if (ROLE_HEARING_VIEWER.equals(roleAssignment.getRoleName())) {
                assertHearingSystemRoleAssignment(
                    actorId, roleAssignment, ROLE_HEARING_VIEWER, jurisdiction, caseType
                );
                foundHearingViewer.set(true);
            } else {
                fail("Unexpected role assignment: " + roleAssignment.getRoleName());
            }
        });
        assertTrue(foundHearingManager.get());
        assertTrue(foundHearingViewer.get());
    }


    protected Request createSystemRoleRequest(String process, String reference) {
        return Request.builder()
            .assignerId(UUID.randomUUID().toString()) // NB: shouldn't matter for System User request
            .process(process)
            .reference(reference)
            .replaceExisting(true) // System User roles should always replace
            .build();
    }

    protected AssignmentRequest createCaseRoleAssignmentRequest(String assignerId,
                                                                String actorId,
                                                                RoleCategory roleCategory,
                                                                String roleName,
                                                                String jurisdiction,
                                                                String caseId) {
        var request = Request.builder()
            .assignerId(assignerId)
            .process("Integration_Test")
            .reference(caseId + "/" + roleName)
            .replaceExisting(true)
            .build();

        Map<String, JsonNode> attributes = new HashMap<>();
        attributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        attributes.put("caseId", convertValueJsonNode(caseId));

        var roleAssignment = RoleAssignment.builder()
            .actorId(actorId)
            .actorIdType(ActorIdType.IDAM)
            .roleType(RoleType.CASE)
            .roleCategory(roleCategory)
            .roleName(roleName)
            .classification(Classification.RESTRICTED)
            .grantType(GrantType.SPECIFIC)
            .attributes(JacksonUtils.convertValue(attributes))
            .build();

        return AssignmentRequest.builder()
            .request(request)
            .requestedRoles(List.of(roleAssignment))
            .build();
    }

    protected RoleAssignment createSystemRoleAssignment(String actorId, String roleName, String jurisdiction) {
        Map<String, JsonNode> attributes = new HashMap<>();
        attributes.put("jurisdiction", convertValueJsonNode(jurisdiction));

        return RoleAssignment.builder()
            .actorId(actorId)
            .actorIdType(ActorIdType.IDAM)
            .roleType(RoleType.ORGANISATION)
            .roleName(roleName)
            .classification(Classification.PUBLIC)
            .grantType(GrantType.STANDARD)
            .roleCategory(RoleCategory.SYSTEM)
            .attributes(JacksonUtils.convertValue(attributes))
            .build();
    }

    protected RoleAssignment createHearingSystemRoleAssignment(String actorId,
                                                               String roleName,
                                                               String jurisdiction,
                                                               String caseType) {
        RoleAssignment roleAssignment = createSystemRoleAssignment(actorId, roleName, jurisdiction);
        // add extra attribute
        roleAssignment.getAttributes().put("caseType", convertValueJsonNode(caseType));
        return roleAssignment;
    }

    protected static String writeValueAsPrettyJson(Object input) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);
    }

}
