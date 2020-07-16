package uk.gov.hmcts.reform.roleassignment.domain.service.getroles;

import static uk.gov.hmcts.reform.roleassignment.apihelper.Constants.UUID_PATTERN;
import static uk.gov.hmcts.reform.roleassignment.v1.V1.Error.NO_RECORDS_FOUND_BY_ACTOR;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.data.cachecontrol.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;
import uk.gov.hmcts.reform.roleassignment.v1.V1;

@Service
public class RetrieveRoleAssignmentOrchestrator {

    private PersistenceService persistenceService;
    private PrepareResponseService prepareResponseService;

    public RetrieveRoleAssignmentOrchestrator(PersistenceService persistenceService,
                                              PrepareResponseService prepareResponseService) {
        this.persistenceService = persistenceService;
        this.prepareResponseService = prepareResponseService;
    }

    //1. call parse request service
    //2. Call retrieve Data service to fetch all required objects
    //3. Call Validation model service to create aggregation objects and apply drools validation rule
    //4. Call persistence to fetch requested assignment records
    //5. Call prepare response to make HATEOUS based response.

    public ResponseEntity<Object> getAssignmentsByActor(String actorId) throws Exception {
        ValidationUtil.validateInputParams(UUID_PATTERN, actorId);
        List<RoleAssignment> assignments = persistenceService.getAssignmentsByActor(UUID.fromString(actorId));
        if (CollectionUtils.isEmpty(assignments)) {
            throw new ResourceNotFoundException(String.format(NO_RECORDS_FOUND_BY_ACTOR + "%s", actorId.toString()));
        }
        return prepareResponseService.prepareRetrieveRoleResponse(
            assignments,
            UUID.fromString(actorId));
    }

    public ResponseEntity<Object> retrieveRoleAssignmentsByActorIdAndCaseId(String actorId, String caseId,
                                                                            String roleType) {
        if (StringUtils.isEmpty(actorId) && StringUtils.isEmpty(caseId)) {
            throw new BadRequestException(V1.Error.INVALID_ACTOR_AND_CASE_ID);
        }

        if (StringUtils.isNotEmpty(actorId)) {
            ValidationUtil.validateInputParams(UUID_PATTERN, actorId);
        }
        if (StringUtils.isNotEmpty(caseId)) {
            ValidationUtil.validateCaseId(caseId);
        }

        List<RoleAssignment> assignmentList =
            persistenceService.getAssignmentsByActorAndCaseId(actorId,caseId, roleType);
        return ResponseEntity.status(HttpStatus.OK).body(assignmentList);
    }

    public long retrieveETag(UUID actorId) throws Exception {
        ActorCacheEntity entity = persistenceService.getActorCacheEntity(actorId);
        return entity.getEtag();
    }
}
