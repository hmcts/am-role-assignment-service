package uk.gov.hmcts.reform.roleassignment.domain.service.getroles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.roleassignment.data.ActorCacheEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.Assignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentResource;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PersistenceService;
import uk.gov.hmcts.reform.roleassignment.domain.service.common.PrepareResponseService;
import uk.gov.hmcts.reform.roleassignment.util.Constants;
import uk.gov.hmcts.reform.roleassignment.util.ValidationUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequestScope
public class RetrieveRoleAssignmentOrchestrator {

    private PersistenceService persistenceService;
    private PrepareResponseService prepareResponseService;

    public RetrieveRoleAssignmentOrchestrator(@Autowired PersistenceService persistenceService,
                                              @Autowired PrepareResponseService prepareResponseService) {
        this.persistenceService = persistenceService;
        this.prepareResponseService = prepareResponseService;
    }

    //1. call parse request service
    //2. Call retrieve Data service to fetch all required objects
    //3. Call Validation model service to create aggregation objects and apply drools validation rule
    //4. Call persistence to fetch requested assignment records
    //5. Call prepare response to make HATEOUS based response.

    public ResponseEntity<RoleAssignmentResource> getAssignmentsByActor(String actorId) {
        ValidationUtil.validateId(Constants.NUMBER_TEXT_HYPHEN_PATTERN, actorId);
        List<? extends Assignment> assignments = persistenceService.getAssignmentsByActor(actorId);
        return prepareResponseService.prepareRetrieveRoleResponse(
            assignments,
            actorId
        );
    }

    public JsonNode getListOfRoles() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        InputStream input = RetrieveRoleAssignmentOrchestrator.class.getClassLoader()
            .getResourceAsStream(Constants.ROLES_JSON);
        assert input != null;
        rootNode = mapper.readTree(input);

        return rootNode;
    }

    public long retrieveETag(String actorId) {
        ActorCacheEntity entity = persistenceService.getActorCacheEntity(actorId);
        return entity.getEtag();
    }
}
