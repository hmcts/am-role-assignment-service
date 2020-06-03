package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.RequestedRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.HashSet;
import java.util.UUID;

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
        requestEntity.setRoleAssignmentHistoryEntities(new HashSet<HistoryEntity>());

        //Prepare History entity
         roleAssignmentRequest.getRequestedRoles().stream().map(roleAssignment -> convertHistoryToEntity(
            roleAssignment,
            requestEntity));


        //Persist the request entity
        requestRepository.save(requestEntity);
    }

    private HistoryEntity convertHistoryToEntity(RequestedRole model, RequestEntity requestEntity) {
        HistoryEntity historyEntity = HistoryEntity.builder().actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(JacksonUtils.convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .status(model.getStatus().toString())
            .readOnly(Boolean.TRUE)
            .requestEntity(requestEntity)
            .build();
        requestEntity.getRoleAssignmentHistoryEntities().add(historyEntity);
        return historyEntity;
    }

    private RequestEntity convertRequestIntoEntity(RoleRequest roleRequest) {

        RequestEntity requestEntity = RequestEntity.builder()
            .correlationId("request1")
            .status(Status.CREATED.toString())
            .process("businessProcess1")
            .reference("abc-3434242")
            .authenticatedUserId(UUID.randomUUID())
            .clientId("sdsd")
            .requesterId(UUID.randomUUID())
            .replaceExisting(Boolean.FALSE)
            .requestType(RequestType.CREATE.toString())
            .build();
         return requestEntity;

    }


}
