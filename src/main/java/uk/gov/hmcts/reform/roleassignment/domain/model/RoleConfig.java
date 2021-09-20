package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.getConfiguredRoles;

@Slf4j
public class RoleConfig {

    @Getter
    private static RoleConfig roleConfig = buildRoleConfig();

    private final Map<String,RoleConfigRole> roleConfigByRoleName = new HashMap<>();

    private RoleConfig(Collection<RoleConfigRole> roles) {
        roles.forEach(r -> roleConfigByRoleName.put(String.join("_", r.getName(), r.getCategory().name()), r));
    }

    public RoleConfigRole get(String roleName, RoleCategory roleCategory) {
        return roleConfigByRoleName.get(String.join("_", roleName, roleCategory.name()));
    }

    /**
     * Copy the role name and category into each of the patterns
     * for the given role.
     */
    private static void setRoleNameAndCategory(RoleConfigRole role) {
        String roleName = role.getName();
        var roleCategory = role.getCategory();
        role.getPatterns().forEach(
            p -> {
                p.setRoleName(roleName);
                p.setRoleCategory(roleCategory);
            });
    }

    private static RoleConfig buildRoleConfig() {
        List<RoleConfigRole> allRoles = getConfiguredRoles();
        allRoles.forEach(RoleConfig::setRoleNameAndCategory);
        return new RoleConfig(allRoles);
    }

}
