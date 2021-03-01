package uk.gov.hmcts.reform.roleassignment.data;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class RoleAssignmentEntitySpecifications {

    private RoleAssignmentEntitySpecifications() {

    }


    public static Specification<RoleAssignmentEntity> searchByActorIds(List<String> actorIds) {
        if (actorIds == null || actorIds.isEmpty()) {
            return null;
        }
        return (root, query, builder) -> builder.or(actorIds
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("actorId"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByGrantType(List<String> grantTypes) {
        if (grantTypes == null || grantTypes.isEmpty()) {
            return null;
        }
        return (root, query, builder) -> builder.or(grantTypes
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("grantType"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByValidDate(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return (root, query, builder) -> builder.and(
            builder.or(builder.lessThanOrEqualTo(root.get("beginTime"), date),root.get("beginTime").isNull()),
            builder.or(builder.greaterThanOrEqualTo(root.get("endTime"), date),root.get("endTime").isNull())

        );

    }

    public static Specification<RoleAssignmentEntity> searchByAttributes(Map<String, List<String>> attributes) {

        if (attributes == null || attributes.isEmpty()) {

            return null;
        }
        return (root, query, builder) -> builder.and(attributes.entrySet()
                                                         .stream()
                                                         .map(entry -> builder.or(entry.getValue()
                                                                               .stream()
                                                     .map(value -> builder.equal(builder.function(
                                                                                   "jsonb_extract_path_text",
                                                                                   String.class,
                                                                       root.<String>get("attributes"),
                                                                       builder.literal(entry.getKey())
                                                                               ), value)).toArray(Predicate[]::new)))
                                                         .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByRoleType(List<String> roleTypes) {

        if (roleTypes == null || roleTypes.isEmpty()) {
            return null;
        }

        return (root, query, builder) -> builder.or(roleTypes
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("roleType"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByRoleName(List<String> roleNames) {

        if (roleNames == null || roleNames.isEmpty()) {
            return null;
        }
        return (root, query, builder) -> builder.or(roleNames
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("roleName"), value))
                                                        .toArray(Predicate[]::new));

    }


    public static Specification<RoleAssignmentEntity> searchByClassification(List<String> classifications) {

        if (classifications == null || classifications.isEmpty()) {
            return null;

        }
        return (root, query, builder) -> builder.or(classifications
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("classification"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByRoleCategories(List<String> roleCategories) {
        if (roleCategories == null || roleCategories.isEmpty()) {
            return null;

        }
        return (root, query, builder) -> builder.or(roleCategories
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("roleCategory"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByAuthorisations(List<String> authorisations) {

        if (CollectionUtils.isEmpty(authorisations)) {
            return null;

        }

        return (root, query, builder) ->
            builder.or(authorisations.stream()
                           .map(element ->
                                builder.isNotNull(
                                   builder.function("array_position", Integer.class,
                                                    root.get("authorisations"),builder.literal(element))

                               )

                           ).toArray(Predicate[]::new));

    }


}
