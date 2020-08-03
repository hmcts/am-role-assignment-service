package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

/*@RunWith(MockitoJUnitRunner.class)
class CreateRoleAssignmentOrchestratorTest {

    @Mock
    private ParseRequestService parseRequestService = mock(ParseRequestService.class);
    @Mock
    private PersistenceService persistenceService = mock(PersistenceService.class);
    @Mock
    private ValidationModelService validationModelService = mock(ValidationModelService.class);
    @Mock
    private PersistenceUtil persistenceUtil = mock(PersistenceUtil.class);
    @Mock
    private PrepareResponseService prepareResponseService = mock(PrepareResponseService.class);


    @InjectMocks
    private CreateRoleAssignmentOrchestrator sut = new CreateRoleAssignmentOrchestrator(
        parseRequestService,
        persistenceService,
        validationModelService,
        persistenceUtil,
        prepareResponseService, createRoleAssignmentService
    );

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createRoleAssignment_ReplaceFalse_AcceptRoleRequests() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(CREATED, APPROVED, false);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        doNothing().when(validationModelService).validateRequest(any());

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();
        //for (RoleAssignment requestedRole : result.getRequestedRoles()) {
        //    assertEquals(Status.APPROVED, requestedRole.getStatus());
        //}

        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(6))
            .persistHistory(any(RoleAssignment.class), any(Request.class));
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }

    @Test
    void createRoleAssignment_ReplaceFalse_RejectRoleRequests() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(REJECTED, LIVE, false);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            assignmentRequest.getRequestedRoles().iterator().next(), requestEntity);

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();

        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        for (RoleAssignment requestedRole : result.getRequestedRoles()) {
            assertEquals(REJECTED, requestedRole.getStatus());
        }

        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(6))
            .persistHistory(any(RoleAssignment.class), any(Request.class));
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }

    //@Test
    void createRoleAssignment_ReplaceTrue_RejectRoleRequests() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     false);
        assignmentRequest.getRequest().setReplaceExisting(true);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());

        when(persistenceService.getAssignmentsByProcess(anyString(),anyString(),anyString()))
            .thenReturn((List<RoleAssignment>) assignmentRequest.getRequestedRoles());

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class))).thenReturn(
            assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        Request result = (Request) response.getBody();

        assertEquals(assignmentRequest.getRequest(), result);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(1))
            .getAssignmentsByProcess(anyString(),anyString(),anyString());
    }

    @Test
    void createRoleAssignment_ReplaceTrue_AcceptRoleRequests_DeleteApproved() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.APPROVED,
                                                                                     false);
        assignmentRequest.getRequest().setReplaceExisting(true);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            TestDataBuilder.buildRoleAssignment(Status.APPROVED), requestEntity);

        when(persistenceService.getAssignmentsByProcess(anyString(),anyString(),anyString()))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection_Updated(Status.APPROVED));

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        //setApprovedStatusByDrool(assignmentRequest, historyEntity);

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();
        //for (RoleAssignment requestedRole : result.getRequestedRoles()) {
        //    assertEquals(HttpStatus.ACCEPTED, requestedRole.getStatus());
        //}

        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(10))
            .persistHistory(any(RoleAssignment.class), any(Request.class));
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }

    @Test
    void createRoleAssignment_ReplaceTrue_AcceptRoleRequests_DeleteRejected() throws Exception {
        AssignmentRequest assignmentRequest = TestDataBuilder.buildAssignmentRequest(Status.CREATED, Status.LIVE,
                                                                                     false);
        assignmentRequest.getRequest().setReplaceExisting(true);
        RequestEntity requestEntity = TestDataBuilder.buildRequestEntity(assignmentRequest.getRequest());
        HistoryEntity historyEntity = TestDataBuilder.buildHistoryIntoEntity(
            TestDataBuilder.buildRoleAssignment(Status.LIVE), requestEntity);

        when(persistenceService.getAssignmentsByProcess(anyString(),anyString(),anyString()))
            .thenReturn((List<RoleAssignment>) TestDataBuilder.buildRequestedRoleCollection_Updated(Status.LIVE));

        when(parseRequestService.parseRequest(any(AssignmentRequest.class), any(RequestType.class)))
            .thenReturn(
                assignmentRequest);
        when(persistenceService.persistRequest(any(Request.class))).thenReturn(requestEntity);
        when(persistenceService.persistHistory(
            any(RoleAssignment.class),
            any(Request.class)
        )).thenReturn(historyEntity);

        when(prepareResponseService.prepareCreateRoleResponse(any()))
            .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(assignmentRequest));

        ResponseEntity<Object> response = sut.createRoleAssignment(assignmentRequest);
        AssignmentRequest result = (AssignmentRequest) response.getBody();
        //for (RoleAssignment requestedRole : result.getRequestedRoles()) {
        //    assertEquals(REJECTED, requestedRole.getStatus());
        //}

        assertEquals(assignmentRequest, result);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        verify(parseRequestService, times(1))
            .parseRequest(any(AssignmentRequest.class), any(RequestType.class));
        verify(persistenceService, times(1))
            .persistRequest(any(Request.class));
        verify(persistenceService, times(6))
            .persistHistory(any(RoleAssignment.class), any(Request.class));
        verify(prepareResponseService, times(1))
            .prepareCreateRoleResponse(any(AssignmentRequest.class));
    }
}*/
