package uk.gov.hmcts.reform.roleassignment;


import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.DeleteAssignmentController;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.Role;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildAttributesFromFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@Provider("am_roleAssignment_deleteAssignment")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}", port = "${PACT_BROKER_PORT:9292}", consumerVersionSelectors = {
    @VersionSelector(tag = "master")})
@TestPropertySource(properties = {"roleassignment.query.size=20", "launchdarkly.sdk.environment=pr",
    "spring.cache.type=none"})
@Import(RoleAssignmentProviderTestConfiguration.class)
@IgnoreNoPactsToVerify
public class DeleteRoleAssignmentProviderTest {

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private DataStoreApi dataStoreApi;

    @Autowired
    private DeleteRoleAssignmentOrchestrator deleteRoleAssignmentOrchestrator;

    private static final String ASSIGNMENT_ID = "704c8b1c-e89b-436a-90f6-953b1dc40157";

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(new DeleteAssignmentController(
            deleteRoleAssignmentOrchestrator
        ));
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    @State({"An actor with provided id is available in role assignment service"})
    public void deleteRoleAssignmentById() throws IOException {
        setInitMockId();
    }

    @State({"An actor with provided process & reference is available in role assignment service"})
    public void deleteRoleAssignmentByPr() throws IOException {
        setInitMockPr();
    }

    private void setInitMockId() throws IOException {
        Request deleteRequest = TestDataBuilder.buildRequest(Status.LIVE, false);
        List<RoleAssignment> roleAssignmentList = TestDataBuilder
            .buildRoleAssignmentList_Custom(Status.LIVE, "1234", "attributesCase.json", RoleType.CASE,
                                            "tribunal-caseworker");

        when(securityUtils.getServiceName()).thenReturn("am_org_role_mapping_service");
        when(persistenceService.persistRequest(any())).thenReturn(TestDataBuilder.buildRequestEntity(deleteRequest));
        when(persistenceService.getAssignmentById(UUID.fromString(ASSIGNMENT_ID)))
            .thenReturn(roleAssignmentList);
        when(persistenceService.getStatusByParam(FeatureFlagEnum.IAC_1_0.getValue(), "pr")).thenReturn(true);
        when(securityUtils.getUserId()).thenReturn("3168da13-00b3-41e3-81fa-cbc71ac28a0f");

        JsonNode attributes = buildAttributesFromFile("attributesCase.json");
        Map<String, JsonNode> attributeMap = JacksonUtils.convertValue(attributes);
        List<Assignment> assignmentList  = new ArrayList<>();
        assignmentList.add(ExistingRoleAssignment.builder().actorId("3168da13-00b3-41e3-81fa-cbc71ac28a0f")
                               .roleType(RoleType.ORGANISATION).roleName("tribunal-caseworker").attributes(attributeMap)
                               .status(Status.APPROVED).build());
        when(persistenceService.persistRequest(any())).thenReturn(createEntity());
        doReturn(assignmentList).when(persistenceService)
            .retrieveRoleAssignmentsByQueryRequest(any(), anyInt(), anyInt(), any(), any(), anyBoolean());
        when(persistenceService.getTotalRecords()).thenReturn(1L);

        when(dataStoreApi.getCaseDataV2(anyString())).thenReturn(Case.builder().id("1212121212121213").jurisdiction(
            "IA").caseTypeId("Asylum").build());

    }

    private void setInitMockPr() throws IOException {
        Request deleteRequest = TestDataBuilder.buildRequest(Status.LIVE, false);
        List<RoleAssignment> roleAssignmentList = TestDataBuilder
            .buildRoleAssignmentList_Custom(Status.LIVE,"1234","attributes.json", RoleType.CASE,
                                            "tribunal-caseworker");

        when(securityUtils.getServiceName()).thenReturn("am_org_role_mapping_service");
        when(persistenceService.persistRequest(any())).thenReturn(TestDataBuilder.buildRequestEntity(deleteRequest));
        when(persistenceService.getAssignmentsByProcess("p2", "r2", Status.LIVE.toString()))
            .thenReturn(roleAssignmentList);
    }

    public RequestEntity createEntity() {
        return RequestEntity.builder()
            .correlationId("123")
            .id(UUID.fromString("c3552563-80e1-49a1-9dc9-b2625e7c44dc"))
            .authenticatedUserId("3168da13-00b3-41e3-81fa-cbc71ac28a0f")
            .clientId("am_org_role_mapping_service")
            .created(LocalDateTime.now())
            .status(Status.APPROVED.toString())
            .build();

    }
}
