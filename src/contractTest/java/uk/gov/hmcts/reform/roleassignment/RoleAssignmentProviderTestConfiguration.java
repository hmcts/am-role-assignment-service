package uk.gov.hmcts.reform.roleassignment;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;


@TestConfiguration
public class RoleAssignmentProviderTestConfiguration {

    @MockBean
    private PersistenceService persistenceService;

    @MockBean
    private PrepareResponseService prepareResponseService;

    @Bean
    @Primary
    public RetrieveRoleAssignmentOrchestrator getListOfRoles() {
        return new RetrieveRoleAssignmentOrchestrator(persistenceService, prepareResponseService);
    }
}
