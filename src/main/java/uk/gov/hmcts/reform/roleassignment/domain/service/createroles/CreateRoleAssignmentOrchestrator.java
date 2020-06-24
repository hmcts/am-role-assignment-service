package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.casedata.DefaultCaseDataRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.RetrieveDataService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.domain.service.security.IdamRoleService;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class CreateRoleAssignmentOrchestrator {

    private DefaultCaseDataRepository caseService;
    private IdamRoleService idamService;
    private RetrieveDataService retrieveDataService;
    private ParseRequestService parseRequestService;
    private PersistenceService persistenceService;
    private ValidationModelService validationModelService;
    private PersistenceUtil persistenceUtil;
    Request request;
    RequestEntity requestEntity;

    public CreateRoleAssignmentOrchestrator(DefaultCaseDataRepository caseService,
                                            IdamRoleService idamService,
                                            RetrieveDataService retrieveDataService,
                                            ParseRequestService parseRequestService,
                                            PersistenceService persistenceService,
                                            ValidationModelService validationModelService,
                                            PersistenceUtil persistenceUtil) {
        this.caseService = caseService;
        this.idamService = idamService;
        this.retrieveDataService = retrieveDataService;
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
        this.validationModelService = validationModelService;
        this.persistenceUtil = persistenceUtil;
    }

    public ResponseEntity<Object> createRoleAssignment(AssignmentRequest roleAssignmentRequest) throws Exception {

        AssignmentRequest existingAssignmentRequest;

        //1. call parse request service
        AssignmentRequest parsedAssignmentRequest = parseRequestService.parseRequest(roleAssignmentRequest, RequestType.CREATE);

        //2. Call persistence service to store only the request
        requestEntity = persistInitialRequest(parsedAssignmentRequest.getRequest());
        requestEntity.setHistoryEntities(new HashSet<>());
        request = parsedAssignmentRequest.getRequest();
        request.setId(requestEntity.getId());

        //Check replace existing true/false
        if (request.isReplaceExisting()) {

            //retrieve existing assignments ans prepared temp request
            existingAssignmentRequest = retrieveExistingAssignments(parsedAssignmentRequest);

            //validation
            evaluateDeleteAssignments(existingAssignmentRequest, parsedAssignmentRequest);

            //Checking all assignments has DELETE_APPROVED status to create new entries of assignment records
            checkAllDeleteApproved(existingAssignmentRequest, parsedAssignmentRequest);

        } else {


            //Save requested role in history table with CREATED and Approved Status
            createNewAssignmentRecords(parsedAssignmentRequest);

            checkAllApproved(parsedAssignmentRequest);


        }


        //8. Call the persistence to copy assignment records to RoleAssignmentLive table
        ResponseEntity<Object> result = PrepareResponseService.prepareCreateRoleResponse(parsedAssignmentRequest);
        parseRequestService.removeCorrelationLog();
        return result;
    }

    @NotNull
    private AssignmentRequest retrieveExistingAssignments(AssignmentRequest parsedAssignmentRequest) {
        AssignmentRequest existingAssignmentRequest;
        List<RequestedRole> existingRoles = persistenceService.getExistingRoleByProcessAndReference(
            request.process,
            request.reference,
            Status.LIVE.toString()
        );

        //create a new existing assignment request for delete records
        existingAssignmentRequest = new AssignmentRequest();
        existingAssignmentRequest.setRequest(parsedAssignmentRequest.getRequest());
        existingAssignmentRequest.setRequestedRoles(existingRoles);
        return existingAssignmentRequest;
    }

    private void evaluateDeleteAssignments(AssignmentRequest existingAssignmentRequest, AssignmentRequest parsedAssignmentRequest) throws Exception {
        //calling drools rules for validation
        validationModelService.validateRequest(existingAssignmentRequest);

        // we are mocking delete rejected status
        checkDeleteApproved(existingAssignmentRequest);


    }

    private void checkAllApproved(AssignmentRequest parsedAssignmentRequest) {

        // decision block
        List<RequestedRole> createApprovedRoles = parsedAssignmentRequest.getRequestedRoles().stream().filter(role -> role.getStatus().equals(
            Status.APPROVED)).collect(
            Collectors.toList());

        if (createApprovedRoles.size() == parsedAssignmentRequest.getRequestedRoles().size()) {
            executeCreateRequest(parsedAssignmentRequest);


        } else {
            rejectCreateRequest(parsedAssignmentRequest);


        }
    }

    private void rejectCreateRequest(AssignmentRequest parsedAssignmentRequest) {
        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status REJECTED
        insertRequestedRole(parsedAssignmentRequest, Status.REJECTED);

        // Update request status to REJECTED
        request.setStatus(Status.REJECTED);
        requestEntity.setStatus(Status.REJECTED.toString());
        persistenceService.persistRequestToHistory(requestEntity);
    }

    private void executeCreateRequest(AssignmentRequest parsedAssignmentRequest) {
        // Insert parsedAssignmentRequest.getRequestedRoles() records into live table
        moveHistoryRecordsToLiveTable(requestEntity);

        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status LIVE
        insertRequestedRole(parsedAssignmentRequest, Status.LIVE);

        // Update request status to approved
        request.setStatus(Status.APPROVED);
        requestEntity.setStatus(Status.APPROVED.toString());
        persistenceService.persistRequestToHistory(requestEntity);
    }

    private void checkAllDeleteApproved(AssignmentRequest existingAssignmentRequest, AssignmentRequest parsedAssignmentRequest
    ) throws Exception {
        // decision block
        List<RequestedRole> deleteApprovedRoles = existingAssignmentRequest.getRequestedRoles().stream().filter(role -> role.getStatus().equals(
            Status.DELETE_APPROVED)).collect(
            Collectors.toList());

        if (deleteApprovedRoles.size() == existingAssignmentRequest.getRequestedRoles().size()) {

            //Create New Assignment records
            createNewAssignmentRecords(parsedAssignmentRequest);

            // decision block
            List<RequestedRole> createApprovedRoles = parsedAssignmentRequest.getRequestedRoles().stream().filter(
                role -> role.getStatus().equals(
                    Status.APPROVED)).collect(
                Collectors.toList());

            if (createApprovedRoles.size() == parsedAssignmentRequest.getRequestedRoles().size()) {

                executeReplaceRequest(existingAssignmentRequest, parsedAssignmentRequest);


            } else {
                rejectReplaceRequest(existingAssignmentRequest, parsedAssignmentRequest);

            }

        } else {


            rejectDeleteRequest(existingAssignmentRequest);

        }
    }

    private void rejectDeleteRequest(AssignmentRequest existingAssignmentRequest) {
        //Insert existingAssignmentRequest.getRequestedRoles() records into history table with status deleted-Rejected
        insertRequestedRole(existingAssignmentRequest, Status.DELETE_REJECTED);
        // Update request status to REJECTED
        request.setStatus(Status.REJECTED);
        requestEntity.setStatus(Status.REJECTED.toString());
        persistenceService.persistRequestToHistory(requestEntity);
    }

    private void rejectReplaceRequest(AssignmentRequest existingAssignmentRequest, AssignmentRequest parsedAssignmentRequest) {
        //Insert existingAssignmentRequest.getRequestedRoles() records into history table with status deleted-Rejected
        insertRequestedRole(existingAssignmentRequest, Status.DELETE_REJECTED);

        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status REJECTED
        insertRequestedRole(parsedAssignmentRequest, Status.REJECTED);

        // Update request status to REJECTED
        request.setStatus(Status.REJECTED);
        requestEntity.setStatus(Status.REJECTED.toString());
        persistenceService.persistRequestToHistory(requestEntity);
    }

    private void executeReplaceRequest(AssignmentRequest existingAssignmentRequest, AssignmentRequest parsedAssignmentRequest) {
        //delete existingAssignmentRequest.getRequestedRoles() records from live table--Hard delete
        deleteExistingLiveRecords(existingAssignmentRequest);

        //Insert existingAssignmentRequest.getRequestedRoles() records into history table with status deleted-soft delete
        insertRequestedRole(existingAssignmentRequest, Status.DELETED);


        // Insert parsedAssignmentRequest.getRequestedRoles() records into live table
        moveHistoryRecordsToLiveTable(requestEntity);

        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status LIVE
        insertRequestedRole(parsedAssignmentRequest, Status.LIVE);

        // Update request status to approved
        request.setStatus(Status.APPROVED);
        requestEntity.setStatus(Status.APPROVED.toString());
        persistenceService.persistRequestToHistory(requestEntity);
    }

    private void checkDeleteApproved(AssignmentRequest existingAssignmentRequest) {
        for (RequestedRole requestedRole : existingAssignmentRequest.getRequestedRoles()) {
            requestedRole.setRequest(existingAssignmentRequest.getRequest());
            if (!requestedRole.getStatus().equals(Status.APPROVED)) {
                requestedRole.status = Status.DELETE_REJECTED;
                requestedRole.statusSequence = Status.DELETE_REJECTED.sequence;
            } else {
                requestedRole.status = Status.DELETE_APPROVED;
                requestedRole.statusSequence = Status.DELETE_APPROVED.sequence;
            }
            // persist history in db
            requestEntity.getHistoryEntities().add(persistenceService.persistHistory(requestedRole, request));

        }

        //Persist request to update relationship with history entities
        persistenceService.persistRequestToHistory(requestEntity);
    }

    //Create New Assignment Records
    private void createNewAssignmentRecords(AssignmentRequest parsedAssignmentRequest) throws Exception {
        //Save new requested role in history table with CREATED Status

        insertRequestedRole(parsedAssignmentRequest, Status.CREATED);

        validationModelService.validateRequest(parsedAssignmentRequest);

        //Save requested role in history table with APPROVED/REJECTED Status
        for (RequestedRole requestedRole : parsedAssignmentRequest.getRequestedRoles()) {
            requestedRole.setRequest(parsedAssignmentRequest.getRequest());
            requestEntity.getHistoryEntities().add(persistenceService.persistHistory(requestedRole, request));
        }

        //Persist request to update relationship with history entities
        persistenceService.persistRequestToHistory(requestEntity);
    }

    private void moveHistoryRecordsToLiveTable(RequestEntity requestEntity) {
        List<HistoryEntity> historyEntities = requestEntity.getHistoryEntities().stream().filter(entity -> entity.getStatus().equals(
            Status.APPROVED.toString())).collect(
            Collectors.toList());

        List<RoleAssignment> roleAssignments = historyEntities.stream().map(entity -> persistenceUtil.convertHistoryEntitiesInRoleAssignment(
            entity)).collect(
            Collectors.toList());
        for (RoleAssignment requestedRole : roleAssignments) {

            requestedRole.setStatus(Status.LIVE);
            persistenceService.persistRoleAssignment(requestedRole);

        }
    }


    private RequestEntity persistInitialRequest(Request request) {

        return persistenceService.persistRequest(request);
    }

    private void deleteExistingLiveRecords(AssignmentRequest existingAssignmentRecords) {

        for (RequestedRole requestedRole : existingAssignmentRecords.getRequestedRoles()) {
            persistenceService.deleteRoleAssignment(requestedRole);

        }

    }

    private void insertRequestedRole(AssignmentRequest existingAssignmentRequest, Status status) {
        for (RequestedRole requestedRole : existingAssignmentRequest.getRequestedRoles()) {
            requestedRole.setRequest(existingAssignmentRequest.getRequest());
            requestedRole.status = status;
            // persist history in db
            HistoryEntity entity = persistenceService.persistHistory(requestedRole, request);
            requestedRole.setId(entity.getId());
            requestEntity.getHistoryEntities().add(entity);

        }

        //Persist request to update relationship with history entities
        persistenceService.persistRequestToHistory(requestEntity);

    }


}
