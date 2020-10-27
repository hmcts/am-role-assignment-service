package uk.gov.hmcts.reform.roleassignment.domain.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.Data;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

@Data
public class RoleConfigPattern {

	private String roleName;
	private RoleCategory roleCategory;
	private final RoleConfigConstraint<RoleType> roleType;
	private final RoleConfigConstraint<GrantType> grantType;
	private final RoleConfigConstraint<Classification> classification;
	private final RoleConfigConstraint<String> beginTime;
	private final RoleConfigConstraint<String> endTime;
	private final Map<String,RoleConfigConstraint<String>> attributes;

	public boolean attributesMatch(Map<String, JsonNode> roleAttributes) {
		return attributes.entrySet().stream().allMatch(entry -> entry.getValue().matches(asText(roleAttributes.get(entry.getKey()))));
	}

	private static String asText(JsonNode jsonNode) {
		return jsonNode == null || jsonNode instanceof NullNode ? null : jsonNode.asText();
	}

	// ----------------------------------------------------- TEST CODE -------------------------------------------------------

	public static void main(String[] args) throws Exception {
		Map<String, RoleConfigConstraint<String>> attributes = new HashMap<>();
		attributes.put("jurisdiction", constraint(true, "DIVORCE", "PROBATE"));
		attributes.put("beginTime", constraint(true));
		attributes.put("region", constraint(false, "NORTH", "SOUTH"));
		RoleConfigPattern pattern = new RoleConfigPattern(null, null, null, null, null, attributes);
		System.out.println(test(true, pattern, "jurisdiction", "DIVORCE", "region", "NORTH", "beginTime", "2020-20-01", "other", "something"));
		System.out.println(test(false, pattern, "region", "NORTH", "beginTime", "2020-20-01", "other", "something"));
		System.out.println(test(false, pattern, "jurisdiction", "WRONG", "region", "NORTH", "beginTime", "2020-20-01", "other", "something"));
		System.out.println(test(true, pattern, "jurisdiction", "DIVORCE", "beginTime", "2020-20-01", "other", "something"));
		System.out.println(test(false, pattern, "jurisdiction", "DIVORCE", "region", "WRONG", "beginTime", "2020-20-01", "other", "something"));
		System.out.println(test(false, pattern, "jurisdiction", "DIVORCE", "region", "NORTH", "other", "something"));
		System.out.println(test(false, pattern, "jurisdiction", "DIVORCE", "region", "NORTH", "beginTime", null, "other", "something"));
	}

	private static boolean test(boolean expected, RoleConfigPattern pattern, String ... roleAttributes) throws Exception {
		StringBuilder builder = new StringBuilder("{");
		String separator = "";
		for (int i = 0; i < roleAttributes.length; i += 2) {
			String quote = roleAttributes[i + 1] != null ? "\"" : "";
			builder.append(separator).append("\"").append(roleAttributes[i]).append("\":").append(quote).append(roleAttributes[i + 1]).append(quote);
			separator = ",";
		}
		builder.append("}");
		ObjectNode attributesNode = (ObjectNode)JacksonUtils.MAPPER.readTree(builder.toString());
		Map<String, JsonNode> attributes = new HashMap<>();
		Iterator<String> fieldNames = attributesNode.fieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			attributes.put(fieldName, attributesNode.get(fieldName));
		}
		boolean result = pattern.attributesMatch(attributes);
		return result == expected;
	}

	@SafeVarargs
	private static <T> RoleConfigConstraint<T> constraint(boolean mandatory, T ... values) {
		RoleConfigConstraint<T> c;
		if (values.length == 0) {
			c = new RoleConfigConstraint<>(mandatory, null);
		} else {
			Set<T> valueSet = new HashSet<>();
			for (T v : values) {
				valueSet.add(v);
			}
			c = new RoleConfigConstraint<>(mandatory, valueSet);
		}
		return c;
	}
}
