package uk.gov.hmcts.reform.roleassignment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.roleassignment.BaseTest;
import uk.gov.hmcts.reform.roleassignment.MockUtils;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.UserRoles;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.launchdarkly.FeatureConditionEvaluation;
import uk.gov.hmcts.reform.roleassignment.oidc.IdamRepository;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@TestPropertySource(properties = {"dbFeature.flags.enable=iac_specific_1_0"})
public class DroolSpecificAccessIntegrationTest extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(DroolSpecificAccessIntegrationTest.class);

    private static final String ASSIGNMENT_ID = "f7edb29d-e421-450c-be66-a10169b04f0a";
    private static final String ACTOR_ID = "123e4567-e89b-42d3-a456-556642445612";
    public static final String CREATED = "CREATED";
    public static final String APPROVED = "APPROVED";
    private static final String AUTHORISED_SERVICE = "ccd_gw";

    private MockMvc mockMvc;
    private JdbcTemplate template;

    @Inject
    private WebApplicationContext wac;

    @MockBean
    private IdamRepository idamRepository;

    @Autowired
    private DataSource ds;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @MockBean
    private IdamRoleService idamRoleService;

    @MockBean
    private RetrieveDataService retrieveDataService;

    @MockBean
    private FeatureConditionEvaluation featureConditionEvaluation;

    @Before
    public void setUp() throws Exception {
        template = new JdbcTemplate(ds);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        MockitoAnnotations.openMocks(this);
        String uid = ACTOR_ID;
        UserRoles roles = UserRoles.builder()
            .uid(uid)
            .roles(Arrays.asList("caseworker", "am-import"))
            .build();

        doReturn(roles).when(idamRoleService).getUserRoles(anyString());
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        doReturn(true).when(featureConditionEvaluation).preHandle(any(),any(),any());
        MockUtils.setSecurityAuthorities(authentication, MockUtils.ROLE_CASEWORKER);
        UserInfo userInfo = UserInfo.builder()
            .uid(uid)
            .sub("emailId@a.com")
            .build();
        doReturn(userInfo).when(idamRepository).getUserInfo(anyString());
        Case retrievedCase = Case.builder().id("1234567890123456")
            .caseTypeId("Asylum")
            .jurisdiction("IA")
            .build();
        doReturn(retrievedCase).when(retrieveDataService).getCaseById(anyString());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts =
        {"classpath:sql/insert_case_role_assignment.sql",
            "classpath:sql/insert_organisation_role_assignment.sql"})
    public void shouldCreateRoleAssignmentsWithSpecificAccess() throws Exception {
        final String url = "/am/role-assignments";

        for (String r : List.of("specific-access-granted", "specific-access-denied")) {
            for (RoleCategory c : List.of(RoleCategory.JUDICIAL, RoleCategory.LEGAL_OPERATIONS, RoleCategory.ADMIN)) {
                AssignmentRequest assignmentRequest = buildAssignmentRequest(r, c);
                logger.info(" assignmentRequest :  {}", mapper.writeValueAsString(assignmentRequest));

                mockMvc.perform(post(url)
                                    .contentType(JSON_CONTENT_TYPE)
                                    .headers(getHttpHeaders("xui_webapp"))
                                    .content(mapper.writeValueAsBytes(assignmentRequest))
                ).andExpect(status().is(201)).andReturn();
            }
        }
    }

    @NotNull
    private AssignmentRequest buildAssignmentRequest(String roleName, RoleCategory category) throws IOException {
        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1234567890123456"));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode("IA"));
        roleAssignmentAttributes.put("caseTypeId", convertValueJsonNode("Asylum"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode("specific access"));

        AssignmentRequest assignmentRequest = TestDataBuilder.createRoleAssignmentRequest(false,
                                                                                          false);
        assignmentRequest.getRequest().setProcess("specific-access");
        assignmentRequest.getRequest().setReference("1234567890123456/" + roleName + "/" + ACTOR_ID);
        assignmentRequest.getRequest().setAssignerId(ACTOR_ID);
        assignmentRequest.getRequestedRoles().forEach(r -> {
            r.setNotes(convertValueJsonNode(List.of("specific")));
            r.setGrantType(GrantType.NONE);
            r.setClassification(Classification.PRIVATE);
            r.setRoleCategory(category);
            r.setRoleName(roleName);
            r.setActorId(ACTOR_ID);
            r.setAttributes(roleAssignmentAttributes);
            r.setReadOnly(true);
        });
        return assignmentRequest;
    }

}
