package uk.gov.hmcts.reform.roleassignment;


import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.DeleteAssignmentController;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@Provider("am_roleAssignment_deleteAssignment")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}", port = "${PACT_BROKER_PORT:9292}", consumerVersionSelectors = {
    @VersionSelector(tag = "master")})
@Import(RoleAssignmentProviderTestConfiguration.class)
@IgnoreNoPactsToVerify
public class DeleteRoleAssignmentProviderTest {

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private SecurityUtils securityUtils;

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
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
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
            .buildRoleAssignmentList_Custom(Status.LIVE,"1234","attributes.json");

        when(securityUtils.getServiceName()).thenReturn("am_org_role_mapping_service");
        when(persistenceService.persistRequest(any())).thenReturn(TestDataBuilder.buildRequestEntity(deleteRequest));
        when(persistenceService.getAssignmentById(UUID.fromString(ASSIGNMENT_ID)))
            .thenReturn(roleAssignmentList);
    }

    private void setInitMockPr() throws IOException {
        Request deleteRequest = TestDataBuilder.buildRequest(Status.LIVE, false);
        List<RoleAssignment> roleAssignmentList = TestDataBuilder
            .buildRoleAssignmentList_Custom(Status.LIVE,"1234","attributes.json");

        when(securityUtils.getServiceName()).thenReturn("am_org_role_mapping_service");
        when(persistenceService.persistRequest(any())).thenReturn(TestDataBuilder.buildRequestEntity(deleteRequest));
        when(persistenceService.getAssignmentsByProcess("p2", "r2", Status.LIVE.toString()))
            .thenReturn(roleAssignmentList);
    }
}
