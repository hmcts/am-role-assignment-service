package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.data.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.Request;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentSubset;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.util.CreatedTimeComparator;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CreateRoleAssignmentOrchestrator {

    private ParseRequestService parseRequestService;
    private PersistenceService persistenceService;
    private ValidationModelService validationModelService;
    private PersistenceUtil persistenceUtil;
    private PrepareResponseService prepareResponseService;
    Request request;
    RequestEntity requestEntity;
    List<UUID> emptyUUIds = new ArrayList<>();
    private static final String LOG_MESSAGE = "Request has been rejected due to following assignment Ids :";

    public CreateRoleAssignmentOrchestrator(ParseRequestService parseRequestService,
                                            PersistenceService persistenceService,
                                            ValidationModelService validationModelService,
                                            PersistenceUtil persistenceUtil,
                                            PrepareResponseService prepareResponseService) {
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
        this.validationModelService = validationModelService;
        this.persistenceUtil = persistenceUtil;
        this.prepareResponseService = prepareResponseService;
    }

    public ResponseEntity<Object> createRoleAssignment(AssignmentRequest roleAssignmentRequest) throws ParseException {

        AssignmentRequest existingAssignmentRequest;

        //1. call parse request service
        AssignmentRequest parsedAssignmentRequest = parseRequestService
            .parseRequest(roleAssignmentRequest, RequestType.CREATE);
        //2. Call persistence service to store only the request
        requestEntity = persistInitialRequest(parsedAssignmentRequest.getRequest());
        requestEntity.setHistoryEntities(new HashSet<>());
        request = parsedAssignmentRequest.getRequest();
        request.setId(requestEntity.getId());

        //Check replace existing true/false
        if (request.isReplaceExisting()) {

            //retrieve existing assignments and prepared temp request
            existingAssignmentRequest = retrieveExistingAssignments(parsedAssignmentRequest);

            // compare identical existing and incoming requested roles based on some attributes
            try {
                if (hasAssignmentsUpdated(existingAssignmentRequest, parsedAssignmentRequest)) {

                    //validation
                    evaluateDeleteAssignments(existingAssignmentRequest);

                    //Checking all assignments has DELETE_APPROVED status to create new entries of assignment records
                    checkAllDeleteApproved(existingAssignmentRequest, parsedAssignmentRequest);
                } else {
                    // Update request status to REJECTED
                    request.setStatus(Status.REJECTED);
                    requestEntity.setStatus(Status.REJECTED.toString());
                    requestEntity.setLog(
                        "The request could not be completed due to a conflict(duplicate)"
                            + " with the current state of the resource");
                    request.setLog(
                        "The request could not be completed due to a conflict(duplicate) "
                            + "with the current state of the resource.");
                    persistenceService.updateRequest(requestEntity);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        request);

                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("context", e);
            }

        } else {
            //Save requested role in history table with CREATED and Approved Status
            createNewAssignmentRecords(parsedAssignmentRequest);
            checkAllApproved(parsedAssignmentRequest);

        }


        //8. Call the persistence to copy assignment records to RoleAssignmentLive table
        ResponseEntity<Object> result = prepareResponseService.prepareCreateRoleResponse(parsedAssignmentRequest);

        parseRequestService.removeCorrelationLog();
        return result;
    }

    @NotNull
    private AssignmentRequest retrieveExistingAssignments(AssignmentRequest parsedAssignmentRequest) {
        AssignmentRequest existingAssignmentRequest;
        List<RoleAssignment> existingAssignments = persistenceService.getAssignmentsByProcess(
            request.getProcess(),
            request.getReference(),
            Status.LIVE.toString()
        );
        CreatedTimeComparator createdTimeComparator = new CreatedTimeComparator();
        existingAssignments.sort(createdTimeComparator);
        //create a new existing assignment request for delete records
        existingAssignmentRequest = new AssignmentRequest(new Request(), Collections.emptyList());
        existingAssignmentRequest.setRequest(parsedAssignmentRequest.getRequest());
        existingAssignmentRequest.setRequestedRoles(existingAssignments);
        return existingAssignmentRequest;
    }

    private void evaluateDeleteAssignments(AssignmentRequest existingAssignmentRequest) {

        //calling drools rules for validation
        validationModelService.validateRequest(existingAssignmentRequest);

        // we are mocking delete rejected status
        checkDeleteApproved(existingAssignmentRequest);


    }

    private void checkAllApproved(AssignmentRequest parsedAssignmentRequest) {

        // decision block
        List<RoleAssignment> createApprovedAssignments = parsedAssignmentRequest.getRequestedRoles().stream()
            .filter(role -> role.getStatus().equals(
                Status.APPROVED)).collect(
                Collectors.toList());

        if (createApprovedAssignments.size() == parsedAssignmentRequest.getRequestedRoles().size()) {
            executeCreateRequest(parsedAssignmentRequest);


        } else {
            List<UUID> rejectedAssignmentIds = parsedAssignmentRequest.getRequestedRoles().stream()
                .filter(role -> role.getStatus().equals(
                    Status.REJECTED)).map(RoleAssignment::getId).collect(
                    Collectors.toList());
            rejectCreateRequest(parsedAssignmentRequest, rejectedAssignmentIds);


        }
    }

    private void rejectCreateRequest(AssignmentRequest parsedAssignmentRequest, List<UUID> rejectedAssignmentIds) {
        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status REJECTED
        insertRequestedRole(parsedAssignmentRequest, Status.REJECTED, rejectedAssignmentIds);

        // Update request status to REJECTED
        request.setStatus(Status.REJECTED);
        requestEntity.setStatus(Status.REJECTED.toString());
        if (!rejectedAssignmentIds.isEmpty()) {
            requestEntity.setLog(LOG_MESSAGE
                                     + rejectedAssignmentIds.toString());
            request.setLog(LOG_MESSAGE
                               + rejectedAssignmentIds.toString());
        }


        persistenceService.updateRequest(requestEntity);
    }

    private void executeCreateRequest(AssignmentRequest parsedAssignmentRequest) {
        // Insert parsedAssignmentRequest.getRequestedRoles() records into live table
        moveHistoryRecordsToLiveTable(requestEntity);

        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status LIVE
        insertRequestedRole(parsedAssignmentRequest, Status.LIVE, emptyUUIds);

        // Update request status to approved
        request.setStatus(Status.APPROVED);
        requestEntity.setLog(request.getLog());
        requestEntity.setStatus(Status.APPROVED.toString());
        persistenceService.updateRequest(requestEntity);
    }

    private void checkAllDeleteApproved(AssignmentRequest existingAssignmentRequest,
                                        AssignmentRequest parsedAssignmentRequest) {
        // decision block
        List<RoleAssignment> deleteApprovedAssignments = existingAssignmentRequest.getRequestedRoles().stream()
            .filter(role -> role.getStatus().equals(
                Status.DELETE_APPROVED)).collect(
                Collectors.toList());

        if (deleteApprovedAssignments.size() == existingAssignmentRequest.getRequestedRoles().size()) {

            //Create New Assignment records
            createNewAssignmentRecords(parsedAssignmentRequest);

            // decision block
            List<RoleAssignment> createApprovedAssignments = parsedAssignmentRequest
                .getRequestedRoles().stream()
                .filter(role -> role.getStatus().equals(
                    Status.APPROVED))
                .collect(Collectors.toList());

            if (createApprovedAssignments.size() == parsedAssignmentRequest.getRequestedRoles().size()) {

                executeReplaceRequest(existingAssignmentRequest, parsedAssignmentRequest);


            } else {
                List<UUID> rejectedAssignmentIds = parsedAssignmentRequest.getRequestedRoles().stream()
                    .filter(role -> role.getStatus().equals(
                        Status.REJECTED)).map(RoleAssignment::getId).collect(
                        Collectors.toList());
                rejectDeleteRequest(existingAssignmentRequest, rejectedAssignmentIds, parsedAssignmentRequest);

            }

        } else {
            List<UUID> rejectedAssignmentIds = existingAssignmentRequest.getRequestedRoles().stream()
                .filter(role -> role.getStatus().equals(
                    Status.DELETE_REJECTED)).map(RoleAssignment::getId).collect(
                    Collectors.toList());


            rejectDeleteRequest(existingAssignmentRequest, rejectedAssignmentIds, parsedAssignmentRequest);

        }
    }

    private void rejectDeleteRequest(AssignmentRequest existingAssignmentRequest,
                                     List<UUID> rejectedAssignmentIds,
                                     AssignmentRequest parsedAssignmentRequest) {
        //Insert existingAssignmentRequest.getRequestedRoles() records into history table with status deleted-Rejected
        insertRequestedRole(existingAssignmentRequest, Status.DELETE_REJECTED, rejectedAssignmentIds);

        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status REJECTED
        insertRequestedRole(parsedAssignmentRequest, Status.REJECTED, rejectedAssignmentIds);
        // Update request status to REJECTED
        request.setStatus(Status.REJECTED);
        requestEntity.setStatus(Status.REJECTED.toString());
        if (!rejectedAssignmentIds.isEmpty()) {
            requestEntity.setLog(LOG_MESSAGE
                                     + rejectedAssignmentIds.toString());
            request.setLog(LOG_MESSAGE
                               + rejectedAssignmentIds.toString());
        }

        persistenceService.updateRequest(requestEntity);
    }


    private void executeReplaceRequest(AssignmentRequest existingAssignmentRequest,
                                       AssignmentRequest parsedAssignmentRequest) {
        //delete existingAssignmentRequest.getRequestedRoles() records from live table--Hard delete
        deleteLiveAssignments(existingAssignmentRequest.getRequestedRoles());

        //Insert existingAssignmentRequest.getRequestedRoles() records into history table with status deleted-soft
        // delete
        insertRequestedRole(existingAssignmentRequest, Status.DELETED, emptyUUIds);


        // Insert parsedAssignmentRequest.getRequestedRoles() records into live table
        moveHistoryRecordsToLiveTable(requestEntity);

        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status LIVE
        insertRequestedRole(parsedAssignmentRequest, Status.LIVE, emptyUUIds);

        // Update request status to approved
        request.setStatus(Status.APPROVED);
        requestEntity.setStatus(Status.APPROVED.toString());
        requestEntity.setLog(request.getLog());
        persistenceService.updateRequest(requestEntity);
    }

    private void checkDeleteApproved(AssignmentRequest existingAssignmentRequest) {
        for (RoleAssignment requestedAssignment : existingAssignmentRequest.getRequestedRoles()) {
            requestedAssignment.setRequest(existingAssignmentRequest.getRequest());
            if (!requestedAssignment.getStatus().equals(Status.APPROVED)) {
                requestedAssignment.setStatus(Status.DELETE_REJECTED);
                requestedAssignment.setStatusSequence(Status.DELETE_REJECTED.sequence);
            } else {
                requestedAssignment.setStatus(Status.DELETE_APPROVED);
                requestedAssignment.setStatusSequence(Status.DELETE_APPROVED.sequence);
            }
            // persist history in db
            requestEntity.getHistoryEntities().add(persistenceService.persistHistory(requestedAssignment, request));

        }

        //Persist request to update relationship with history entities
        persistenceService.updateRequest(requestEntity);
    }

    //Create New Assignment Records
    private void createNewAssignmentRecords(AssignmentRequest parsedAssignmentRequest) {
        //Save new requested role in history table with CREATED Status

        insertRequestedRole(parsedAssignmentRequest, Status.CREATED, emptyUUIds);

        validationModelService.validateRequest(parsedAssignmentRequest);

        //Save requested role in history table with APPROVED/REJECTED Status
        for (RoleAssignment requestedAssignment : parsedAssignmentRequest.getRequestedRoles()) {
            requestedAssignment.setRequest(parsedAssignmentRequest.getRequest());
            requestEntity.getHistoryEntities().add(persistenceService.persistHistory(requestedAssignment, request));
        }

        //Persist request to update relationship with history entities
        persistenceService.updateRequest(requestEntity);
    }

    private void moveHistoryRecordsToLiveTable(RequestEntity requestEntity) {
        List<HistoryEntity> historyEntities = requestEntity.getHistoryEntities()
            .stream()
            .filter(entity -> entity.getStatus().equals(
                Status.APPROVED.toString()))
            .collect(Collectors.toList());

        List<RoleAssignment> roleAssignments = historyEntities.stream().map(entity -> persistenceUtil
            .convertHistoryEntityToRoleAssignment(
                entity)).collect(
            Collectors.toList());
        for (RoleAssignment requestedAssignment : roleAssignments) {
            requestedAssignment.setStatus(Status.LIVE);
            persistenceService.persistRoleAssignment(requestedAssignment);
            persistenceService.persistActorCache(requestedAssignment);
        }
    }


    private RequestEntity persistInitialRequest(Request request) {
        return persistenceService.persistRequest(request);
    }

    private void deleteLiveAssignments(Collection<RoleAssignment> existingAssignments) {
        for (RoleAssignment requestedRole : existingAssignments) {
            persistenceService.deleteRoleAssignment(requestedRole);
            persistenceService.persistActorCache(requestedRole);
        }
    }

    private void insertRequestedRole(AssignmentRequest assignmentRequest,
                                     Status status,
                                     List<UUID> rejectedAssignmentIds) {
        for (RoleAssignment requestedAssignment : assignmentRequest.getRequestedRoles()) {
            requestedAssignment.setRequest(assignmentRequest.getRequest());
            if (!rejectedAssignmentIds.isEmpty()
                && (status.equals(Status.REJECTED)
                || status.equals(Status.DELETE_REJECTED))
                && (requestedAssignment.getStatus().equals(Status.APPROVED)
                || requestedAssignment.getStatus().equals(Status.CREATED)
                || requestedAssignment.getStatus().equals(Status.DELETE_APPROVED))) {
                requestedAssignment.setLog(
                    "Requested Role has been rejected due to following new/existing assignment Ids :"
                        + rejectedAssignmentIds.toString());
            }
            requestedAssignment.setStatus(status);
            // persist history in db
            HistoryEntity entity = persistenceService.persistHistory(requestedAssignment, request);
            requestedAssignment.setId(entity.getId());
            requestEntity.getHistoryEntities().add(entity);
        }
        //Persist request to update relationship with history entities
        persistenceService.updateRequest(requestEntity);
    }

    private boolean hasAssignmentsUpdated(AssignmentRequest existingAssignmentRequest,
                                          AssignmentRequest parsedAssignmentRequest)
        throws InvocationTargetException, IllegalAccessException {

        // convert existing assignment records into role assignment subset
        Map<UUID, RoleAssignmentSubset> existingRecords = JacksonUtils.convertExistingRolesIntoSubSet(
            existingAssignmentRequest);

        // convert incoming assignment records into role assignment subset
        Set<RoleAssignmentSubset> incomingRecords = JacksonUtils.convertRequestedRolesIntoSubSet(
            parsedAssignmentRequest);

        //find common Role Assignment Map
        Map<UUID, RoleAssignmentSubset> commonRecords = findCommonRoleAssignments(existingRecords, incomingRecords);

        // find to create new assignment records or delete existing records.
        Object[] obj = identifyRoleAssignments(
            existingRecords,
            incomingRecords,
            commonRecords,
            parsedAssignmentRequest
        );

        // prepare tempList from incoming requested roles
        if (obj != null && obj.getClass().isArray()) {

            Map<UUID, RoleAssignmentSubset> needToDeleteRoleAssignments = (Map<UUID, RoleAssignmentSubset>) obj[0];
            Set<RoleAssignmentSubset> needToCreateRoleAssignments = (Set<RoleAssignmentSubset>) obj[1];
            return !needToDeleteRoleAssignments.isEmpty() || !needToCreateRoleAssignments.isEmpty();

        }
        return false;


    }

    private Map<UUID, RoleAssignmentSubset> findCommonRoleAssignments(Map<UUID, RoleAssignmentSubset> existingRecords, Set<RoleAssignmentSubset> incomingRecords) {

        Map<UUID, RoleAssignmentSubset> commonRoleAssignments = new Hashtable<>();
        existingRecords.forEach((K, V) -> {
            if (incomingRecords.contains(V)) {
                commonRoleAssignments.put(K, V);
            }
        });

        return commonRoleAssignments;

    }

    private Object[] identifyRoleAssignments(Map<UUID, RoleAssignmentSubset> existingRecords,
                                             Set<RoleAssignmentSubset> incomingRecords,
                                             Map<UUID, RoleAssignmentSubset> commonRecords,
                                             AssignmentRequest parsedAssignmentRequest) {

        Map<UUID, RoleAssignmentSubset> needToDeleteRoleAssignments = null;
        Set<RoleAssignmentSubset> needToCreateRoleAssignments = null;
        ResponseEntity responseEntity = null;

        if (!commonRecords.isEmpty() && !incomingRecords.isEmpty() && !existingRecords.isEmpty()) {
            needToDeleteRoleAssignments = existingRecords.entrySet().stream().filter(e -> !(e.getValue().equals(
                commonRecords.get(e.getKey())))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            needToCreateRoleAssignments = findCreateRoleAssignments(incomingRecords, commonRecords);

        } else if (commonRecords.isEmpty() && !incomingRecords.isEmpty() && (!existingRecords.isEmpty() || existingRecords.isEmpty())) {
            needToCreateRoleAssignments = incomingRecords;
        } else if (commonRecords.isEmpty() && incomingRecords.isEmpty() && !existingRecords.isEmpty()) {
            needToDeleteRoleAssignments = existingRecords;
        } else if (commonRecords.isEmpty() && incomingRecords.isEmpty() && existingRecords.isEmpty()) {
            parsedAssignmentRequest.getRequest().setStatus(Status.REJECTED);
            for (RoleAssignment roleAssignment : parsedAssignmentRequest.getRequestedRoles()) {
                roleAssignment.setStatus(Status.REJECTED);
            }
            responseEntity = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                parsedAssignmentRequest);
        }

        return new Object[]{needToDeleteRoleAssignments, needToCreateRoleAssignments, responseEntity};


    }

    private Set<RoleAssignmentSubset> findCreateRoleAssignments(Set<RoleAssignmentSubset> incomingRecords,
                                                                Map<UUID, RoleAssignmentSubset> commonRecords) {

        Set<RoleAssignmentSubset> needToCreateRoleAssignment = new HashSet<>();

        commonRecords.forEach((K, V) -> {
            if (!incomingRecords.contains(V)) {
                needToCreateRoleAssignment.add(V);

            }
        });

        return needToCreateRoleAssignment;

    }
}



/*
// Scenario 1 existing having 1,2,3 and new having 2,3,4 then final persist records 2,3,4 and 1 should be delete
// Scenario 2 existing having 1,2,3 and new having 4,5,6 then final persist records 1,2,3,4,5,6 and no delete
// Scenario 3 existing having 1,2,3 and new having empty records then existing records will remove.
// Scenario 4 existing having no records and new having 4,5,6 then final persist records 4,5,6
// Scenario 5 existing having no records and new is also empty then final persist records - 400
// Scenario 11 existing having 1,2,3 and new having 1,2,3 then no create but send list of existing records as live with 201
E=ExistingRoleAssignments
N=NewRoleAssignments
C=CommonRoleAssignments
S1={E,N,C} ==> E={1,2,3}; N={2,3,4}; C={2,3} ===> {2,3,4}
S2={E,N}   ==> E={1,2,3}; N={4,5,6}; C={} ===> {1,2,3,4,5,6}
S3={E,C}   ==> E={1,2,3}; N={}; C={} ===> {}
S4={N,C}   ==> E={}; N={4,5,6}; C={} ===> {4,5,6}
S5={E}     ==> E={1,2,3}; N={}; C={} ===> {}
S6={N}     ==> E={}; N={4,5,6}; C={} ===> {4,5,6}
S7={C}     ==> E={}; N={}; C={} ===> {}
S8={}      ==> E={}; N={}; C={} ===> {}
S1=E={1,2,3}; N={2,3,4}; C={2,3} ===> {2,3,4}
S2=E={1,2,3}; N={4,5,6}; C={} ===> {1,2,3,4,5,6}
S3=E={1,2,3}; N={}; C={} ===> {}
S4=E={}; N={4,5,6}; C={} ===> {4,5,6}
S5=E={}; N={}; C={} ===> {}
*********Existing flow in create orchestrator*********
ExistingRoleAssignments(1,2,3)
existingAssignmentsMap{(1,S1),(2,S2),(3,S3)}
newRoleAssignments(R2,R3,R4)
newRoleAssignmentsSubset(S2,S3,S4)
CommonRoleAssignmentsMap{(2,S2),(3,S3)}
CommonRoleAssignmentsMap(S2,S3)
If CommonRoleAssignmentsMap is not empty && newRoleAssignmentsSubset is not empty && ExistingRoleAssignments is not empty then{
    need2RemoveAssignmentMap = existingAssignmentsMap - CommonRoleAssignmentsMap => {(1,S1)}
    need2CreateNewAssignmentSet = newRoleAssignmentsSubset - CommonRoleAssignmentsSet => (S4)
  } elseIf CommonRoleAssignmentsMap is empty && newRoleAssignmentsSubset is not empty && (ExistingRoleAssignments is not empty || ExistingRoleAssignments is empty) {
      need2CreateNewAssignmentSet = newRoleAssignmentsSubset
  } elseIf CommonRoleAssignmentsMap is empty && newRoleAssignmentsSubset is empty && ExistingRoleAssignments is not empty{
      need2RemoveAssignmentMap = existingAssignmentsMap
  } elseIf CommonRoleAssignmentsMap is empty && newRoleAssignmentsSubset is empty && ExistingRoleAssignments is empty {
    //return 422 with rejection saying there is no records to replace and create.
  }
}
If need2CreateNewAssignmentSet is not Empty || need2RemoveAssignmentMap.values is not Empty ==> true{
   //update the existingAssignmentRequest with Only need to be removed record
   //existingAssignmentRequest = existingAssignmentRequest with filter where id = need2RemoveAssignmentMap.Id ==> R1
   //validation
   evaluateDeleteAssignments(existingAssignmentRequest);
   //update the parsedAssignmentRequest with Only new record
   parsedAssignmentRequest = parsedAssignmentRequest with Filter where subset(R) = S4 ==> R4
   //Checking all assignments has DELETE_APPROVED status to create new entries of assignment records
   checkAllDeleteApproved(existingAssignmentRequest, parsedAssignmentRequest, );
} else {
    // Update the parsedAssignmentRequest to contain all the existing records and return with 201.
    parsedAssignmentRequest.getRequestedRoles() = existingAssignmentRequest.getRequestedRoles()
    ResponseEntity<Object> result = prepareResponseService.prepareCreateRoleResponse(parsedAssignmentRequest);
    parseRequestService.removeCorrelationLog();
    return result;
}
 */
