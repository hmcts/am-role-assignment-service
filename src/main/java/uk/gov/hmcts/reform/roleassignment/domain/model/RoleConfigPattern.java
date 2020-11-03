package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Data
public class RoleConfigPattern {

    private String roleName;
    private RoleCategory roleCategory;
    private final RoleConfigConstraint<RoleType> roleType;
    private final RoleConfigConstraint<GrantType> grantType;
    private final RoleConfigConstraint<Classification> classification;
    private final RoleConfigConstraint<String> beginTime;
    private final RoleConfigConstraint<String> endTime;
    private final Map<String, RoleConfigConstraint<String>> attributes;

    public boolean attributesMatch(Map<String, JsonNode> roleAttributes) {
        return attributes.entrySet().stream().allMatch(entry -> entry.getValue().matches(asText(roleAttributes.get(entry.getKey()))));
    }

    private static String asText(JsonNode jsonNode) {
        return jsonNode == null || jsonNode instanceof NullNode ? null : jsonNode.asText();
    }

}
