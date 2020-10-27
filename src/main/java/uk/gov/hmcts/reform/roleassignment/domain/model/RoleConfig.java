package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.io.IOException;
import java.util.HashMap;

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
 +	 * Copy the role name and category into each of the patterns
 +	 * for the given role.
 +	 */
    	private static void setRoleNameAndCategory(RoleConfigRole role) {
        		String roleName = role.getName();
        		RoleCategory roleCategory = role.getCategory();
        		role.getPatterns().forEach(
            				p -> {
                					p.getData().setRoleName(roleName);
                					p.getData().setRoleCategory(roleCategory);
                				});
        	}

       private static RoleConfig buildRoleConfig() {
               InputStream input = JacksonUtils.class.getClassLoader().getResourceAsStream("role.json");
                CollectionType listType = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(
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

    	// ----------------------------------------------------- TEST CODE -------------------------------------------------------

        public static void main(String[] args) {
            	System.out.println(getRoleConfig().get("judge"));
            }
}
