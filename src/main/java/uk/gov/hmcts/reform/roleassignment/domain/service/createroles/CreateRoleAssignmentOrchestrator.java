package uk.gov.hmcts.reform.roleassignment.domain.service.createroles;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.UnprocessableEntityException;
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
import java.util.HashMap;
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
    Map<UUID, RoleAssignmentSubset> needToDeleteRoleAssignments = new HashMap<>();
    Set<RoleAssignmentSubset> needToCreateRoleAssignments = new HashSet<>();
    Set<RoleAssignment> needToRetainRoleAssignments = new HashSet<>();
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

        AssignmentRequest existingAssignmentRequest = null;

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

                    //update the existingAssignmentRequest with Only need to be removed record
                    if (!needToDeleteRoleAssignments.isEmpty()) {
                        updateExistingAssignmentWithNewDeleteRoleAssignments(existingAssignmentRequest);
                    }

                    //update the parsedAssignmentRequest with Only new record
                    if (!needToCreateRoleAssignments.isEmpty()) {
                        updateParseRequestWithNewCreateRoleAssignments(
                            existingAssignmentRequest,
                            parsedAssignmentRequest
                        );


                    } else {
                        parsedAssignmentRequest.setRequestedRoles(Collections.emptyList());

                    }

                    //Checking all assignments has DELETE_APPROVED status to create new entries of assignment records
                    checkAllDeleteApproved(existingAssignmentRequest, parsedAssignmentRequest);

                } else {
                    duplicateRequest(existingAssignmentRequest, parsedAssignmentRequest);

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
        if (!needToCreateRoleAssignments.isEmpty() && needToRetainRoleAssignments.size() > 0) {
            parsedAssignmentRequest.getRequestedRoles().addAll(needToRetainRoleAssignments);
        } else if (needToRetainRoleAssignments.size() > 0) {
            parsedAssignmentRequest.setRequestedRoles(needToRetainRoleAssignments);
        }


        ResponseEntity<Object> result = prepareResponseService.prepareCreateRoleResponse(parsedAssignmentRequest);

        parseRequestService.removeCorrelationLog();
        return result;
    }

    private ResponseEntity<Object> duplicateRequest(AssignmentRequest existingAssignmentRequest,
                                                    AssignmentRequest parsedAssignmentRequest) {
        parsedAssignmentRequest.getRequest().setStatus(Status.APPROVED);
        requestEntity.setStatus(Status.APPROVED.toString());
        requestEntity.setLog(
            "Duplicate Request: Requested Assignments are already live.");
        request.setLog(
            "Duplicate Request: Requested Assignments are already live.");

        persistenceService.updateRequest(requestEntity);

        //replace new assignments with details of existing and return 201
        parsedAssignmentRequest.setRequestedRoles(existingAssignmentRequest.getRequestedRoles());


        ResponseEntity<Object> result = prepareResponseService.prepareCreateRoleResponse(
            parsedAssignmentRequest);
        parseRequestService.removeCorrelationLog();
        return result;
    }

    private void updateParseRequestWithNewCreateRoleAssignments(AssignmentRequest existingAssignmentRequest,
                                                                AssignmentRequest parsedAssignmentRequest)
        throws IllegalAccessException, InvocationTargetException {

        Set<RoleAssignment> newRoleAssignments = new HashSet<>();
        for (RoleAssignment roleAssignment : parsedAssignmentRequest.getRequestedRoles()) {
            RoleAssignmentSubset roleAssignmentSubset = RoleAssignmentSubset.builder().build();
            BeanUtils.copyProperties(roleAssignmentSubset, roleAssignment);

            if (needToCreateRoleAssignments.contains(roleAssignmentSubset)) {
                newRoleAssignments.add(roleAssignment);
            }
        }

        //replace parsedAssignmentRequest with new role assignments that need to be created
        parsedAssignmentRequest.setRequestedRoles(newRoleAssignments);

        if (needToDeleteRoleAssignments.isEmpty()
            && existingAssignmentRequest != null
            && existingAssignmentRequest.getRequestedRoles().size() > 0) {
            needToRetainRoleAssignments.addAll(existingAssignmentRequest.getRequestedRoles());

        }
    }

    private void updateExistingAssignmentWithNewDeleteRoleAssignments(AssignmentRequest existingAssignmentRequest) {
        List<RoleAssignment> roleAssignmentList = existingAssignmentRequest.getRequestedRoles().stream().filter(
            e -> needToDeleteRoleAssignments.containsKey(
                e.getId())).collect(Collectors.toList());
        needToRetainRoleAssignments = existingAssignmentRequest.getRequestedRoles().stream()
            .filter(e -> !roleAssignmentList.contains(
            e)).collect(
            Collectors.toSet());

        existingAssignmentRequest.setRequestedRoles(roleAssignmentList);
        //validation
        evaluateDeleteAssignments(existingAssignmentRequest);
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
        if (!needToDeleteRoleAssignments.isEmpty()) {
            List<RoleAssignment> deleteApprovedAssignments = existingAssignmentRequest.getRequestedRoles().stream()
                .filter(role -> role.getStatus().equals(
                    Status.DELETE_APPROVED)).collect(
                    Collectors.toList());


            if (deleteApprovedAssignments.size() == existingAssignmentRequest.getRequestedRoles().size()) {

                //Create New Assignment records
                if (!needToCreateRoleAssignments.isEmpty()) {
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
                    //It will delete existing records from db.
                    deleteExistingRecordsWhenNeedToCreateEmpty(existingAssignmentRequest);
                }

            } else {
                List<UUID> rejectedAssignmentIds = existingAssignmentRequest.getRequestedRoles().stream()
                    .filter(role -> role.getStatus().equals(
                        Status.DELETE_REJECTED)).map(RoleAssignment::getId).collect(
                        Collectors.toList());


                rejectDeleteRequest(existingAssignmentRequest, rejectedAssignmentIds, parsedAssignmentRequest);

            }
        } else {
            //Save requested role in history table with CREATED and Approved Status
            createNewAssignmentRecords(parsedAssignmentRequest);
            checkAllApproved(parsedAssignmentRequest);
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
        deleteRecords(existingAssignmentRequest);


        // Insert parsedAssignmentRequest.getRequestedRoles() records into live table
        moveHistoryRecordsToLiveTable(requestEntity);

        // Insert parsedAssignmentRequest.getRequestedRoles() records into history table with status LIVE
        insertRequestedRole(parsedAssignmentRequest, Status.LIVE, emptyUUIds);

        // Update request status to approved
        updateRequestStatus();
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
        identifyRoleAssignments(
            existingRecords,
            incomingRecords,
            commonRecords
        );

        // prepare tempList from incoming requested roles
        return !needToDeleteRoleAssignments.isEmpty() || !needToCreateRoleAssignments.isEmpty();


    }

    private Map<UUID, RoleAssignmentSubset> findCommonRoleAssignments(Map<UUID,
        RoleAssignmentSubset> existingRecords, Set<RoleAssignmentSubset> incomingRecords) {
        Map<UUID, RoleAssignmentSubset> commonRoleAssignments = new Hashtable<>();
        existingRecords.forEach((K, V) -> {
            if (incomingRecords.contains(V)) {
                commonRoleAssignments.put(K, V);
            }
        });

        return commonRoleAssignments;

    }

    private void identifyRoleAssignments(Map<UUID, RoleAssignmentSubset> existingRecords,
                                         Set<RoleAssignmentSubset> incomingRecords,
                                         Map<UUID, RoleAssignmentSubset> commonRecords) {


        if (!commonRecords.isEmpty() && !incomingRecords.isEmpty() && !existingRecords.isEmpty()) {
            needToDeleteRoleAssignments = existingRecords.entrySet().stream().filter(e -> !(e.getValue().equals(
                commonRecords.get(e.getKey())))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            needToCreateRoleAssignments = findCreateRoleAssignments(incomingRecords, commonRecords);

        } else if (commonRecords.isEmpty() && !incomingRecords.isEmpty() && (!existingRecords.isEmpty())) {
            needToCreateRoleAssignments = incomingRecords;
            needToDeleteRoleAssignments = existingRecords;
        } else if (commonRecords.isEmpty() && !incomingRecords.isEmpty() && (existingRecords.isEmpty())) {
            needToCreateRoleAssignments = incomingRecords;
        } else if (commonRecords.isEmpty() && incomingRecords.isEmpty() && !existingRecords.isEmpty()) {
            needToDeleteRoleAssignments = existingRecords;
        } else if (commonRecords.isEmpty() && incomingRecords.isEmpty() && existingRecords.isEmpty()) {
            throw new UnprocessableEntityException("Create with replace existing can not be processed "
                                                       + "without new assignment records");
        }


    }

    private Set<RoleAssignmentSubset> findCreateRoleAssignments(Set<RoleAssignmentSubset> incomingRecords,
                                                                Map<UUID, RoleAssignmentSubset> commonRecords) {

        Set<RoleAssignmentSubset> needToCreateRoleAssignment = new HashSet<>();
        Set<RoleAssignmentSubset> commonRecordsSet = commonRecords.values().stream().collect(Collectors.toSet());

        incomingRecords.forEach((K) -> {
            if (!commonRecordsSet.contains(K)) {
                needToCreateRoleAssignment.add(K);

            }
        });

        return needToCreateRoleAssignment;

    }

    private void deleteExistingRecordsWhenNeedToCreateEmpty(AssignmentRequest existingAssignmentRequest) {
        deleteRecords(existingAssignmentRequest);
        // Update request status to approved
        updateRequestStatus();
    }

    private void updateRequestStatus() {
        request.setStatus(Status.APPROVED);
        requestEntity.setStatus(Status.APPROVED.toString());
        requestEntity.setLog(request.getLog());
        persistenceService.updateRequest(requestEntity);
    }

    private void deleteRecords(AssignmentRequest existingAssignmentRequest) {
        //delete existingAssignmentRequest.getRequestedRoles() records from live table--Hard delete
        deleteLiveAssignments(existingAssignmentRequest.getRequestedRoles());

        //Insert existingAssignmentRequest.getRequestedRoles() records into history table with status deleted-soft
        // delete
        insertRequestedRole(existingAssignmentRequest, Status.DELETED, emptyUUIds);
    }
}




