package uk.gov.hmcts.reform.roleassignment1.domain.service.security;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment1.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment1.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IdamRoleService {

    private Map<String, List<RoleAssignment>> existingRoleByActorId = new HashMap<>();
    SecurityUtils securityUtils;

    public void getRequestedUserId() {
        securityUtils.getUserId();
    }

    public void getRequestUserRole() {
        securityUtils.getUserRolesHeader();
    }


    public Collection<RoleAssignment> getIdamRoleAssignmentsForActor(String actorId) throws Exception {
        List<RoleAssignment> existingRolesForActor = existingRoleByActorId.get(actorId);
        return existingRolesForActor == null ? new ArrayList<>() : existingRolesForActor;
    }
}
