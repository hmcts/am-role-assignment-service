package uk.gov.hmcts.reform.roleassignment.domain.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Value;

@Value
public class RoleConfigConstraint<T> {

	private final boolean mandatory;
	private final Set<T> values;

	public boolean matches(T value) {
		if (mandatory) {
			return
					value != null && (values == null || values.contains(value));
		} else {
			return
					value == null || values == null || values.contains(value);
		}
	}

	// ----------------------------------------------------- TEST CODE -------------------------------------------------------

	public static void main(String[] args) {
		System.out.println(test(true, "A", true, "A", "B"));
		System.out.println(test(true, "A", true));
		System.out.println(test(false, "A", true, "C", "D"));
		System.out.println(test(false, null, true, "C", "D"));
		System.out.println(test(false, null, true));
		System.out.println(test(true, "A", false, "A", "B"));
		System.out.println(test(true, "A", false));
		System.out.println(test(false, "A", false, "C", "D"));
		System.out.println(test(true, null, false, "C", "D"));
		System.out.println(test(true, null, false));
	}

	private static boolean test(boolean expected, String value, boolean mandatory, String ... values) {
		RoleConfigConstraint<String> c;
		if (values.length == 0) {
			c = new RoleConfigConstraint<>(mandatory, null);
		} else {
			Set<String> valueSet = new HashSet<>();
			for (String v : values) {
				valueSet.add(v);
			}
			c = new RoleConfigConstraint<>(mandatory, valueSet);
		}
		boolean result = c.matches(value);
		return result == expected;
	}
}
