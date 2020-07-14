package uk.gov.hmcts.reform.assignment.domain.service.getroles;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.assignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.assignment.data.cachecontrol.ActorCacheEntity;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.assignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.assignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.assignment.util.ValidationUtil;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.assignment.apihelper.Constants.UUID_PATTERN;
import static uk.gov.hmcts.reform.assignment.v1.V1.Error.NO_RECORDS_FOUND_BY_ACTOR;

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

    public long retrieveETag(UUID actorId) throws Exception {
        ActorCacheEntity entity = persistenceService.getActorCacheEntity(actorId);
        return entity.getEtag();
    }
}
