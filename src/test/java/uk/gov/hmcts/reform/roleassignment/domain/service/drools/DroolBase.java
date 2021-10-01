package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfig;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.CREATE_REQUESTED;

public abstract class DroolBase {

    StatelessKieSession kieSession;
    AssignmentRequest assignmentRequest;
    List<Object> facts;
    List<FeatureFlag> featureFlags;

    private final RetrieveDataService retrieveDataService = mock(RetrieveDataService.class);

    @BeforeEach
    public void setUp() {

        //list of facts
        facts = new ArrayList<>();
        featureFlags = new ArrayList<>();

        //build assignmentRequest
        assignmentRequest = getAssignmentRequest()
            .build();

        // facts must contain the role config, for access to the patterns
        facts.add(RoleConfig.getRoleConfig());

        //mock the retrieveDataService to fetch the Case Object
        Case caseObj = Case.builder().id("1234567890123456")
            .caseTypeId("Asylum")
            .jurisdiction("IA")
            .build();
        doReturn(caseObj).when(retrieveDataService).getCaseById("1234567890123456");

        //mock the retrieveDataService to fetch the Case Object with incorrect type ID
        Case caseObj1 = Case.builder().id("1234567890123457")
            .caseTypeId("Not Asylum")
            .jurisdiction("IA")
            .build();
        doReturn(caseObj1).when(retrieveDataService).getCaseById("1234567890123457");

        //mock the retrieveDataService to fetch the Case Object with incorrect Jurisdiction ID
        Case caseObj2 = Case.builder().id("1234567890123458")
            .caseTypeId("Asylum")
            .jurisdiction("Not IA")
            .build();
        doReturn(caseObj2).when(retrieveDataService).getCaseById("1234567890123458");

        // Set up the rule engine for validation.
        KieServices ks = KieServices.Factory.get();
        KieContainer kieContainer = ks.getKieClasspathContainer();
        this.kieSession = kieContainer.newStatelessKieSession("role-assignment-validation-session");
        this.kieSession.setGlobal("DATA_SERVICE", retrieveDataService);

    }

    private AssignmentRequest.AssignmentRequestBuilder getAssignmentRequest() {
        return AssignmentRequest.builder().request(Request.builder()
                                                       .id(UUID.fromString("ab4e8c21-27a0-4abd-aed8-810fdce22adb"))
                                                       .authenticatedUserId("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                                                       .correlationId("38a90097-434e-47ee-8ea1-9ea2a267f51d")
                                                       .assignerId("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                                                       .requestType(RequestType.CREATE)
                                                       .reference("4772dc44-268f-4d0c-8f83-f0fb662aac84")
                                                       .process(("p2"))
                                                       .replaceExisting(true)
                                                       .status(Status.CREATED)
                                                       .created(ZonedDateTime.now())
                                                       .build());


    }

    RoleAssignment getRequestedCaseRole(RoleCategory roleCategory, String roleName, GrantType grantType) {
        return RoleAssignment.builder()
            .id(UUID.randomUUID())
            .actorId(UUID.randomUUID().toString())
            .actorIdType(ActorIdType.IDAM)
            .roleCategory(roleCategory)
            .roleType(RoleType.CASE)
            .roleName(roleName)
            .grantType(grantType)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(CREATE_REQUESTED)
            .attributes(new HashMap<String, JsonNode>())
            .build();
    }

    RoleAssignment getRequestedCaseRole(RoleCategory roleCategory, String roleName, GrantType grantType,
                                        String attributeKey, String attributeVal, Status status) {
        RoleAssignment ra = RoleAssignment.builder()
            .id(UUID.randomUUID())
            .actorId(UUID.randomUUID().toString())
            .actorIdType(ActorIdType.IDAM)
            .roleCategory(roleCategory)
            .roleType(RoleType.CASE)
            .roleName(roleName)
            .grantType(grantType)
            .classification(Classification.PUBLIC)
            .readOnly(true)
            .status(status)
            .attributes(new HashMap<String, JsonNode>())
            .build();
        ra.setAttribute(attributeKey, attributeVal);
        return ra;
    }

    RoleAssignment getRequestedCaseRole_2(RoleCategory roleCategory,
                                          String roleName,
                                          GrantType grantType,
                                          RoleType roleType,
                                          Classification classification,
                                          Status status) {
        return RoleAssignment.builder()
            .id(UUID.randomUUID())
            .actorId(UUID.randomUUID().toString())
            .actorIdType(ActorIdType.IDAM)
            .roleCategory(roleCategory)
            .roleType(roleType)
            .roleName(roleName)
            .grantType(grantType)
            .classification(classification)
            .readOnly(true)
            .status(status)
            .attributes(new HashMap<String, JsonNode>())
            .build();
    }

    void buildExecuteKieSession() {
        // facts must contain the request
        facts.add(assignmentRequest.getRequest());
        facts.addAll(featureFlags);
        // facts must contain all affected role assignments
        facts.addAll(assignmentRequest.getRequestedRoles());
        // Run the rules
        kieSession.execute(facts);


    }


}
