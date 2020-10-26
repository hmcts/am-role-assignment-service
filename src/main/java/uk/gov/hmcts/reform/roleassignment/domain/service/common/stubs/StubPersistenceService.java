package uk.gov.hmcts.reform.roleassignment.domain.service.common.stubs;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;

import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

public class StubPersistenceService {

	private static final Map<String, List<RoleAssignment>> ROLE_ASSIGNMENTS_BY_ACTOR_ID = loadRoleAssignments();
	private static final List<RoleAssignment> EMPTY = Collections.unmodifiableList(new ArrayList<>());

	/**
	 * Load role assignment data from the  "existing-role-assignments.json" resource.
	 */
	private static Map<String, List<RoleAssignment>> loadRoleAssignments() {
		try {
			List<RoleAssignment> allRoleAssignments;
			try (InputStream input = StubRetrieveDataService.class.getResourceAsStream("existing-role-assignments.json")) {
				JavaType type = JacksonUtils.MAPPER.getTypeFactory().constructCollectionType(List.class, RoleAssignment.class);
				allRoleAssignments = JacksonUtils.MAPPER.readValue(input, type);
			}
			Map<String, List<RoleAssignment>> roleAssignmentsByActorId = new HashMap<>();
			allRoleAssignments.forEach(
					ra -> {
						String actorId = ra.getActorId();
						List<RoleAssignment> roleAssignments = roleAssignmentsByActorId.get(actorId);
						if (roleAssignments == null) {
							roleAssignments = new ArrayList<>();
							roleAssignmentsByActorId.put(actorId, roleAssignments);
						}
						roleAssignments.add(ra);
					});
			return roleAssignmentsByActorId;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to load stub role assignment data.", t);
		}
	}

    public List<RoleAssignment> getAssignmentsByActor(String actorId) {
    	List<RoleAssignment> roleAssignments = ROLE_ASSIGNMENTS_BY_ACTOR_ID.get(actorId);
    	return roleAssignments == null ? EMPTY : Collections.unmodifiableList(roleAssignments);
	}

	public static void main(String[] args) throws Exception {
    	System.out.println(new StubPersistenceService().getAssignmentsByActor("00000001"));
    }
}
