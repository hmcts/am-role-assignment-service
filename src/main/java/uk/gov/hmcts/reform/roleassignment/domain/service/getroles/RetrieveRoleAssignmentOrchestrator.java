package uk.gov.hmcts.reform.roleassignment.domain.service.getroles;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.data.cachecontrol.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRole;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ParseRequestService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.ValidationModelService;
import uk.gov.hmcts.reform.roleassignment.util.PersistenceUtil;

import java.util.List;
import java.util.UUID;

@Service
public class RetrieveRoleAssignmentOrchestrator {

    private ParseRequestService parseRequestService;
    private PersistenceService persistenceService;
    private ValidationModelService validationModelService;
    private PersistenceUtil persistenceUtil;

    public RetrieveRoleAssignmentOrchestrator(ParseRequestService parseRequestService,
                                              PersistenceService persistenceService,
                                              ValidationModelService validationModelService,
                                              PersistenceUtil persistenceUtil) {
        this.parseRequestService = parseRequestService;
        this.persistenceService = persistenceService;
        this.validationModelService = validationModelService;
        this.persistenceUtil = persistenceUtil;
    }

    //1. call parse request service
    //2. Call retrieve Data service to fetch all required objects
    //3. Call Validation model service to create aggregation objects and apply drools validation rule
    //4. Call persistence to fetch requested assignment records
    //5. Call prepare response to make HATEOUS based response.

    public ResponseEntity<Object> retrieveRoleAssignmentByActorId(String actorId)
        throws Exception {
        parseRequestService.parseActorId(actorId);
        List<ExistingRole> roles = persistenceService.getExistingRoleAssignment(UUID.fromString(actorId));
        if (CollectionUtils.isEmpty(roles)) {
            throw new ResourceNotFoundException(actorId.toString());
        }
        ResponseEntity<Object> result = PrepareResponseService.prepareRetrieveRoleResponse(
            roles,
            UUID.fromString(actorId)
        );
        return result;
    }

    public long retrieveETag(UUID actorId) throws Exception {
        ActorCacheEntity entity = persistenceService.getActorCacheEntity(actorId);
        return entity.getEtag();
    }
}
