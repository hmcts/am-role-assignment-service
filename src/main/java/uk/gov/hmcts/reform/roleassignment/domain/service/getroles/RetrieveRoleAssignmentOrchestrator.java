package uk.gov.hmcts.reform.roleassignment.domain.service.getroles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.controller.advice.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;
import uk.gov.hmcts.reform.roleassignment.v1.V1;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

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

    public ResponseEntity<Object> getAssignmentsByActor(String actorId) {
        ValidationUtil.validateInputParams(Constants.UUID_PATTERN, actorId);
        List<RoleAssignment> assignments = persistenceService.getAssignmentsByActor(UUID.fromString(actorId));
        if (CollectionUtils.isEmpty(assignments)) {
            throw new ResourceNotFoundException(String.format(
                V1.Error.NO_RECORDS_FOUND_BY_ACTOR + " %s",
                actorId));
        }
        return prepareResponseService.prepareRetrieveRoleResponse(
            assignments,
            UUID.fromString(actorId));
    }

    public JsonNode getListOfRoles() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try (InputStream input = RetrieveRoleAssignmentOrchestrator.class.getClassLoader()
            .getResourceAsStream(Constants.ROLES_JSON)) {
            assert input != null;
            rootNode = mapper.readTree(input);
            for (JsonNode roleNode: rootNode) {
                ObjectNode obj = (ObjectNode) roleNode;
                obj.remove(Constants.ROLE_JSON_PATTERNS_FIELD);
            }
        } catch (Exception e) {
            throw new ServiceException("Service Exception", e);
        }
        return rootNode;
    }

    public long retrieveETag(UUID actorId) {
        ActorCacheEntity entity = persistenceService.getActorCacheEntity(actorId);
        return entity.getEtag();
    }
}
