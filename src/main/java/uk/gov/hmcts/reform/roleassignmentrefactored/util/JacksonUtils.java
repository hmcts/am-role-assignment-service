package uk.gov.hmcts.reform.roleassignmentrefactored.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import lombok.Getter;
import org.apache.commons.beanutils.BeanUtils;
import uk.gov.hmcts.reform.roleassignmentrefactored.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.Role;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignmentrefactored.domain.model.RoleAssignmentSubset;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@Singleton
public class JacksonUtils {

    private JacksonUtils() {
    }

    @Getter
    private static final Map<String, List<Role>> configuredRoles = new HashMap<>();

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

    public static final TypeReference<HashMap<String, JsonNode>> getHashMapTypeReference() {
        return new TypeReference<HashMap<String, JsonNode>>() {
        };
    }

    public static List<RoleAssignmentSubset> convertRequestedRolesIntoSubSet(AssignmentRequest assignmentRequest)
        throws InvocationTargetException, IllegalAccessException {
        RoleAssignmentSubset subset = null;
        List<RoleAssignmentSubset> roleAssignmentSubsets = new ArrayList<>();
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            subset = RoleAssignmentSubset.builder().build();
            BeanUtils.copyProperties(subset, roleAssignment);
            roleAssignmentSubsets.add(subset);
        }

        return roleAssignmentSubsets;

    }

    static {

        try (InputStream input = JacksonUtils.class.getClassLoader().getResourceAsStream("role.json")) {
            CollectionType listType = MAPPER.getTypeFactory().constructCollectionType(
                ArrayList.class,
                Role.class
            );
            List<Role> allRoles = MAPPER.readValue(input, listType);
            configuredRoles.put("roles", allRoles);

        } catch (Exception e) {
            throw new ServiceException("Service Exception", e);
        }

    }
}
