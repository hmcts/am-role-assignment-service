package uk.gov.hmcts.reform.roleassignment;

import static org.mockito.ArgumentMatchers.any;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.CreateAssignmentController;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.roleassignment.util.CorrelationInterceptorUtil;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("am_role_assignment_service_create")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}", host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:9292}")
@Import(RoleAssignmentProviderTestConfiguration.class)
public class CreateRoleAssignmentProviderTest {

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private CorrelationInterceptorUtil correlationInterceptorUtil;

    @Autowired
    private CreateRoleAssignmentOrchestrator createRoleAssignmentOrchestrator;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(new CreateAssignmentController(
            createRoleAssignmentOrchestrator
        ));
        context.setTarget(testTarget);

    }

    @State({"The assignment request is valid with one requested role and replaceExisting flag as false"})
    public void createRoleAssignmentReplaceExistingFalse() {
        setInitiMock();
    }

    private void setInitiMock() {

        when(persistenceService.persistRequest(any())).thenReturn(createEntity());
        when(securityUtils.getUserId()).thenReturn("14a21569-eb80-4681-b62c-6ae2ed069e2f");
        when(correlationInterceptorUtil.preHandle(any())).thenReturn("14a21569-eb80-4681-b62c-6ae2ed069e2d");
    }

    public RequestEntity createEntity() {
        return RequestEntity.builder()
            .correlationId("123")
            .id(UUID.fromString("c3552563-80e1-49a1-9dc9-b2625e7c44dc"))
            .authenticatedUserId("123")
            .clientId("123")
            .created(LocalDateTime.now())
            .build();

    }
}
