package uk.gov.hmcts.reform.roleassignment.domain.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

@Slf4j
public class RoleConfig {

    @Getter
    private static RoleConfig roleConfig = buildRoleConfig();

    private final Map<String,RoleConfigRole> roleConfigByRoleName = new HashMap<>();

    private RoleConfig(Collection<RoleConfigRole> roles) {
        roles.forEach(r -> roleConfigByRoleName.put(r.getName(), r));
    }

    public RoleConfigRole get(String roleName) {
        return roleConfigByRoleName.get(roleName);
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
        InputStream input = JacksonUtils.class.getClassLoader().getResourceAsStream("role.json");
        var listType = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(
            ArrayList.class,
            RoleConfigRole.class
        );
        List<RoleConfigRole> allRoles = new ArrayList<>();
        try {
            allRoles = JacksonUtils.MAPPER.readValue(input, listType);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        allRoles.forEach(RoleConfig::setRoleNameAndCategory);
        return new RoleConfig(allRoles);
    }


}
