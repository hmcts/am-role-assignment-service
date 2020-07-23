package uk.gov.hmcts.reform.roleassignment.auditlog.aop;


/*public class AuditAspectTest {
    private static final String CASE_REFERENCE = "a_test_Case";
    private static final String JURISDICTION = "PROBATE";
    private static final String CASE_TYPE = "a_CaseType";
    private static final String EVENT_NAME = "a_event";
    private static final String USER_ID = "a_targetIdamId";

    private final AuditAspect aspect = new AuditAspect();
    private TestController controllerProxy;

    @Before
    public void setUp() {
        AspectJProxyFactory aspectJProxyFactory = new AspectJProxyFactory(new TestController());
        aspectJProxyFactory.addAspect(aspect);

        DefaultAopProxyFactory proxyFactory = new DefaultAopProxyFactory();
        AopProxy aopProxy = proxyFactory.createAopProxy(aspectJProxyFactory);

        controllerProxy = (TestController) aopProxy.getProxy();
    }

    @Test
    public void shouldPopulateAuditContext() {
        CaseDetails result = controllerProxy.getCase(CASE_REFERENCE, EVENT_NAME);
        assertThat(result).isNotNull();

        AuditContext context = AuditContextHolder.getAuditContext();

        assertThat(context).isNotNull();
        assertThat(context.getCaseId()).isEqualTo(CASE_REFERENCE);
        assertThat(context.getJurisdiction()).isEqualTo(JURISDICTION);
        assertThat(context.getCaseType()).isEqualTo(CASE_TYPE);
        assertThat(context.getEventName()).isEqualTo(EVENT_NAME);

    }

    @Test
    public void shouldPopulateAuditContextEvenMethodExecutionReturnsError() {

        assertThrows(RuntimeException.class, () -> controllerProxy.createCase(CASE_TYPE, USER_ID));

        AuditContext context = AuditContextHolder.getAuditContext();

        assertThat(context).isNotNull();
        assertThat(context.getCaseType()).isEqualTo(CASE_TYPE);
        assertThat(context.getTargetIdamId()).isEqualTo(USER_ID);
        assertThat(context.getCaseId()).isNull();

    }

    @Controller
    @SuppressWarnings("unused")
    public static class TestController {

        public static final String JURISDICTION = "PROBATE";

        @LogAudit(operationType = AuditOperationType.CASE_ACCESSED, caseId
        = "#reference",caseType = "#result.caseTypeId",
            jurisdiction = "#result.jurisdiction", eventName = "#eventName")
        public CaseDetails getCase(String reference, String eventName) {
            CaseDetails caseDetails = new CaseDetails();
            caseDetails.setJurisdiction(JURISDICTION);
            caseDetails.setCaseTypeId(CASE_TYPE);
            return caseDetails;
        }

        @LogAudit(operationType = AuditOperationType.CREATE_CASE, caseType
        = "#caseType", targetIdamId = "#targetIdamId")
        public CaseDetails createCase(String caseType, String targetIdamId) {
            throw new RuntimeException("get case failed");
        }
    }
}*/
