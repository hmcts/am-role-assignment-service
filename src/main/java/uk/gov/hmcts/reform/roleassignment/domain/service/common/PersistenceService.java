package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleRequest;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersistenceService {
    //1. StoreRequest which will insert records in request and history table with log,
    //2. Insert new Assignment record with updated Status in historyTable
    //3. Update Request Status
    //4. Store live record in the roleAssignmentTable

    private HistoryRepository historyRepository;
    private RequestRepository requestRepository;
    private RoleAssignmentRepository roleAssignmentRepository;

    public PersistenceService(HistoryRepository historyRepository, RequestRepository requestRepository, RoleAssignmentRepository roleAssignmentRepository) {
        this.historyRepository = historyRepository;
        this.requestRepository = requestRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistRequest(RoleAssignmentRequest roleAssignmentRequest) {

        //Prepare request entity
        RequestEntity requestEntity = convertRequestIntoEntity(roleAssignmentRequest.getRoleRequest());


        //Prepare History entity
        Set<HistoryEntity> historyEntities = roleAssignmentRequest.getRequestedRoles().stream().map(roleAssignment -> convertHistoryToEntity(
            roleAssignment,
            requestEntity
        )).collect(Collectors.toSet());

        requestEntity.setHistoryEntities(historyEntities);

        //Persist the request entity
        requestRepository.save(requestEntity);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertHistoryWithUpdatedStatus(RoleAssignment roleAssignment) {

        //Persist the history entity
        historyRepository.save(convertHistoryToEntity(
            roleAssignment,
            convertRequestIntoEntity(roleAssignment.getRoleRequest())
        ));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistRoleAssignment(RoleAssignment roleAssignment) {

        //Persist the role assignment entity
        roleAssignmentRepository.save(convertRoleAssignmentToEntity(roleAssignment));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateRequest(RequestEntity requestEntity) {

        //need to pass same request entity which we saved in first step with updated status
        //Update the status of request entity which already saved in db
        requestRepository.save(requestEntity);
    }


    private HistoryEntity convertHistoryToEntity(RoleAssignment model, RequestEntity requestEntity) {
        return HistoryEntity.builder().actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .status(model.getStatus().toString())
            .readOnly(model.readOnly)
            .requestEntity(requestEntity)
            .build();


    }

    private RequestEntity convertRequestIntoEntity(RoleRequest roleRequest) {
        return RequestEntity.builder()
            .correlationId(roleRequest.getCorrelationId())
            .status(roleRequest.getStatus().toString())
            .process(roleRequest.getProcess())
            .reference(roleRequest.getProcess())
            .authenticatedUserId(UUID.fromString(roleRequest.getAuthenticatedUserId()))
            .clientId(roleRequest.getClientId())
            .requesterId(UUID.fromString(roleRequest.getRequestorId()))
            .replaceExisting(roleRequest.replaceExisting)
            .requestType(roleRequest.getRequestType().toString())
            .log(roleRequest.getLog())
            .build();

    }

    private RoleAssignmentEntity convertRoleAssignmentToEntity(RoleAssignment model) {
        return RoleAssignmentEntity.builder().actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .readOnly(model.readOnly)
            .build();


    }


}
