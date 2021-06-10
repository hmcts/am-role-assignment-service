package uk.gov.hmcts.reform.roleassignment.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import uk.gov.hmcts.reform.roleassignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignmentSubset;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleConfigRole;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Named
@Singleton
public class JacksonUtils {

    private JacksonUtils(){
    }

    @Getter
    private static final Map<String, List<RoleConfigRole>> configuredRoles = new HashMap<>();

    public static final JsonFactory jsonFactory = JsonFactory.builder()
        // Change per-factory setting to prevent use of `String.intern()` on symbols
        .disable(JsonFactory.Feature.INTERN_FIELD_NAMES)
        .build();

    public static final ObjectMapper MAPPER = JsonMapper.builder(jsonFactory)
        .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        .build();


    public static Map<String, JsonNode> convertValue(Object from) {
        return MAPPER.convertValue(from, new TypeReference<HashMap<String, JsonNode>>() {
        });
    }

    public static JsonNode convertValueJsonNode(Object from) {
        return MAPPER.convertValue(from, JsonNode.class);
    }

    public static TypeReference<HashMap<String, JsonNode>> getHashMapTypeReference() {
        return new TypeReference<HashMap<String, JsonNode>>() {
        };
    }

    //Find Subset for Incoming Records
    public static Set<RoleAssignmentSubset> convertRequestedRolesIntoSubSet(AssignmentRequest assignmentRequest)
        throws InvocationTargetException, IllegalAccessException {
        RoleAssignmentSubset subset;
        Set<RoleAssignmentSubset> roleAssignmentSubsets = new HashSet<>();
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            subset = RoleAssignmentSubset.builder().build();
            BeanUtils.copyProperties(subset, roleAssignment);
            roleAssignmentSubsets.add(subset);
        }

        return roleAssignmentSubsets;

    }

    //Find Subset for Existing  Records
    public static Map<UUID, RoleAssignmentSubset> convertExistingRolesIntoSubSet(AssignmentRequest assignmentRequest)
        throws InvocationTargetException, IllegalAccessException {
        RoleAssignmentSubset subset;
        Map<UUID, RoleAssignmentSubset> roleAssignmentSubsets = new HashMap<>();
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            subset = RoleAssignmentSubset.builder().build();
            BeanUtils.copyProperties(subset, roleAssignment);
            if (roleAssignment.getRoleType().equals(RoleType.CASE)) {
                //Remove the caseType and jurisdiction entries as it was added by application.
                subset.getAttributes().remove("jurisdiction");
                subset.getAttributes().remove("caseType");
            }
            roleAssignmentSubsets.put(roleAssignment.getId(), subset);
        }

        return roleAssignmentSubsets;

    }

    static {

        InputStream input = JacksonUtils.class.getClassLoader().getResourceAsStream("role.json");
        CollectionType listType = MAPPER.getTypeFactory().constructCollectionType(
            ArrayList.class,
            RoleConfigRole.class
        );
        List<RoleConfigRole> allRoles = null;
        try {
            allRoles = MAPPER.readValue(input, listType);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        configuredRoles.put("roles", allRoles);


    }
}
