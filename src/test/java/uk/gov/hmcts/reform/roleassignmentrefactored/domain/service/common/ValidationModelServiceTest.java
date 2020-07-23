package uk.gov.hmcts.reform.roleassignmentrefactored.domain.service.common;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.Role;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignmentrefactored.helper.TestDataBuilder;
import uk.gov.hmcts.reform.roleassignmentrefactored.util.SecurityUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ValidationModelServiceTest {

    StatelessKieSession kieSessionMock = mock(StatelessKieSession.class);

    IdamRoleService idamRoleServiceMock = mock(IdamRoleService.class);

    RetrieveDataService retrieveDataServiceMock = mock(RetrieveDataService.class);

    SecurityUtils securityUtilsMock = mock(SecurityUtils.class);

    AssignmentRequest assignmentRequest;

    @Getter
    private static final Map<String, List<Role>> configuredRoles = new HashMap<>();

    @InjectMocks
    ValidationModelService sut = new ValidationModelService(kieSessionMock, idamRoleServiceMock,
                                                            retrieveDataServiceMock, securityUtilsMock);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void validateRequest() throws IOException {

        assignmentRequest = TestDataBuilder
            .buildAssignmentRequest(Status.CREATED, Status.LIVE, false);

        sut.validateRequest(assignmentRequest);

        Mockito.verify(kieSessionMock, times(1)).setGlobal("retrieveDataService",
                                                           retrieveDataServiceMock);
    }
}
