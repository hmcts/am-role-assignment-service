package uk.gov.hmcts.reform.roleassignment.domain.service.security;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IdamRoleService {

    private Map<String, List<RoleAssignment>> existingRoleByActorId = new HashMap<>();
    SecurityUtils securityUtils;

    public String getRequestedUserId() {
        return securityUtils.getUserId();
    }

    public String getRequestUserRole() {
        return securityUtils.getUserRolesHeader();
    }


    public Collection<RoleAssignment> getIdamRoleAssignmentsForActor(String actorId) throws Exception {
        List<RoleAssignment> existingRolesForActor = existingRoleByActorId.get(actorId);
        return existingRolesForActor == null ? new ArrayList<>() : existingRolesForActor;
    }
}
