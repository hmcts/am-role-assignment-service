package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.ExistingRole;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IdamRoleService {

    private Map<String, List<ExistingRole>> existingRoleByActorId = new HashMap<>();
    SecurityUtils securityUtils;

    public void getRequestedUserId() {
        securityUtils.getUserId();
    }

    public void getRequestUserRole() {
        securityUtils.getUserRolesHeader();
    }


    public Collection<ExistingRole> getIdamRoleAssignmentsForActor(String actorId) throws Exception {
        List<ExistingRole> existingRolesForActor = existingRoleByActorId.get(actorId);
        return existingRolesForActor == null ? new ArrayList<>() : existingRolesForActor;
    }
}
