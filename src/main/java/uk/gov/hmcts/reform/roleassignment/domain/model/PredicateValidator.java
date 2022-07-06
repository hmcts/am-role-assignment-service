package uk.gov.hmcts.reform.roleassignment.domain.model;

import java.util.function.Predicate;

public class PredicateValidator {

    private PredicateValidator() {
    }

    public static Predicate<String> stringCheckPredicate(String value) {

        return name -> name.equalsIgnoreCase(value);
    }
}
