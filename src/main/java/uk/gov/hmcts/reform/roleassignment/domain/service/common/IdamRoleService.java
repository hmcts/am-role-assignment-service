package uk.gov.hmcts.reform.roleassignment.domain.service.common;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IdamRoleService {

    private Map<String, List<ExistingRole>> EXISTING_ROLES_BY_ACTOR_ID = new HashMap<>();

    public Collection<ExistingRole> getIdamRoleAssignmentsForActor(String actorId) throws Exception
    {
        List<ExistingRole> existingRolesForActor = EXISTING_ROLES_BY_ACTOR_ID.get(actorId);
        return existingRolesForActor == null ? new ArrayList<>() : existingRolesForActor;
    }
}
