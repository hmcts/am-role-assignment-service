package uk.gov.hmcts.reform.roleassignment;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.roleassignment.config.EnvironmentConfiguration;
import uk.gov.hmcts.reform.roleassignment.data.FlagConfig;
import uk.gov.hmcts.reform.roleassignment.data.FlagConfigRepository;
import uk.gov.hmcts.reform.roleassignment.data.DatabseChangelogLockRepository;
import uk.gov.hmcts.reform.roleassignment.data.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.domain.service.createroles.CreateRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.deleteroles.DeleteRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.getroles.RetrieveRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.domain.service.queryroles.QueryRoleAssignmentOrchestrator;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;
import uk.gov.hmcts.reform.roleassignment.util.CorrelationInterceptorUtil;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import static org.mockito.Mockito.when;

@TestConfiguration
@ImportAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    FlywayAutoConfiguration.class
})
public class RoleAssignmentProviderTestConfiguration {

    @Bean
    @Primary
    public PrepareResponseService getPrepareResponseService() {
        return new PrepareResponseService();
    }

    @Bean
    @Primary
    public CorrelationInterceptorUtil correlationInterceptorUtil() {
        return Mockito.mock(CorrelationInterceptorUtil.class);
    }

    @Bean
    @Primary
    public ParseRequestService getParseRequestService() {
        return new ParseRequestService();
    }

    private KieServices kieServices = KieServices.Factory.get();

    @Bean
    public KieContainer kieContainer() {
        return kieServices.getKieClasspathContainer();
    }

    @Bean
    @Primary
    public SecurityUtils securityUtils() {
        return Mockito.mock(SecurityUtils.class);
    }


    @Bean
    public StatelessKieSession getStatelessKieSession() {
        return kieContainer().newStatelessKieSession("role-assignment-validation-session");
    }

    @Bean
    @Primary
    public DataStoreApi dataStoreApi() {
        return Mockito.mock(DataStoreApi.class);
    }

    @Bean
    @Primary
    public CacheManager cacheManager() {
        return Mockito.mock(CacheManager.class);
    }

    @Bean
    @Primary
    public RoleAssignmentRepository roleAssignmentRepository() {
        return Mockito.mock(RoleAssignmentRepository.class);
    }

    @Bean
    @Primary
    public RequestRepository requestRepository() {
        return Mockito.mock(RequestRepository.class);
    }

    @Bean
    @Primary
    public HistoryRepository historyRepository() {
        return Mockito.mock(HistoryRepository.class);
    }

    @Bean
    @Primary
    public DatabseChangelogLockRepository databseChangelogLockRepository() {
        return Mockito.mock(DatabseChangelogLockRepository.class);
    }

    @Bean
    @Primary
    public FlagConfigRepository flagConfigRepository() {
        FlagConfigRepository mock = Mockito.mock(FlagConfigRepository.class);
        Mockito.when(mock.findByFlagNameAndEnv(Mockito.anyString(), Mockito.anyString()))
            .thenAnswer(invocation -> FlagConfig.builder()
                .id(1L)
                .flagName(invocation.getArgument(0))
                .env(invocation.getArgument(1))
                .serviceName("contract-test")
                .status(Boolean.TRUE)
                .build());
        Mockito.when(mock.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
        return mock;
    }

    @Bean
    @Primary
    public RetrieveDataService getRetrieveDataService() {
        return new RetrieveDataService(dataStoreApi(), cacheManager());
    }

    @Bean
    @Primary
    public ValidationModelService getValidationModelService() {
        return new ValidationModelService(getStatelessKieSession(),
                                          getRetrieveDataService(),
                                          persistenceService(),
                                          getEnvironmentConfiguration());
    }

    @Bean
    @Primary
    public PersistenceUtil getPersistenceUtil() {
        return new PersistenceUtil();
    }

    @Bean
    @Primary
    public RetrieveRoleAssignmentOrchestrator getListOfRoles() {
        return new RetrieveRoleAssignmentOrchestrator(persistenceService(), getPrepareResponseService());
    }

    @Bean
    @Primary
    public CreateRoleAssignmentOrchestrator createRoleAssignment() {
        return new CreateRoleAssignmentOrchestrator(getParseRequestService(), getPrepareResponseService(),
                                                    persistenceService(), getValidationModelService(),
                                                    getPersistenceUtil()
        );
    }

    @Bean
    @Primary
    public QueryRoleAssignmentOrchestrator retrieveRoleAssignmentsByQueryRequest() {
        return new QueryRoleAssignmentOrchestrator(persistenceService());
    }

    @Bean
    @Primary
    public DeleteRoleAssignmentOrchestrator deleteRoleAssignment() {
        return new DeleteRoleAssignmentOrchestrator(persistenceService(), getParseRequestService(),
                                                    getValidationModelService(), getPersistenceUtil()
        );
    }

    @Bean
    @Primary
    public PersistenceService persistenceService() {
        return Mockito.mock(PersistenceService.class);
    }

    @Bean
    @Primary
    public jakarta.persistence.EntityManager entityManager() {
        return Mockito.mock(jakarta.persistence.EntityManager.class);
    }

    @Bean
    @Primary
    public EnvironmentConfiguration getEnvironmentConfiguration() {
        EnvironmentConfiguration environmentConfiguration = Mockito.mock(EnvironmentConfiguration.class);
        when(environmentConfiguration.getEnvironment()).thenReturn("pr");
        return environmentConfiguration;
    }

}
