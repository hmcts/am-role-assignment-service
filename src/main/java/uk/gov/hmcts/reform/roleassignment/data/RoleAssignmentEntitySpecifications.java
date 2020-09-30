package uk.gov.hmcts.reform.roleassignment.data;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public  class RoleAssignmentEntitySpecifications {


    public static Specification<RoleAssignmentEntity> searchByActorIds(List<UUID> actorIds) {
        return (root, query, builder) -> builder.or(actorIds
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("actorId"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByGrantType(List<String> gtantTypes) {
        return (root, query, builder) -> builder.or(gtantTypes
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("grantType"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByValidDate(LocalDateTime date) {

        return (root, query, builder) -> builder.and(
            builder.lessThanOrEqualTo(root.get("beginTime"), date),
            builder.greaterThanOrEqualTo(root.get("endTime"), date)
        );

    }

    public static Specification<RoleAssignmentEntity> searchByAttributes(Map<String, List<String>> attributes) {


        return (root, query, builder) -> builder.and(attributes.entrySet()
                                                         .stream()
                                                         .map(entry -> {
                                                             return builder.or(entry.getValue()
                                                                                   .stream()
                                                                                   .map(value -> {
                                                                                       return builder.equal(builder.function(
                                                                                           "jsonb_extract_path_text",
                                                                                           String.class,
                                                                                           root.<String>get("attributes"),
                                                                                           builder.literal(entry.getKey())
                                                                                       ), value);

                                                                                   }).toArray(Predicate[]::new));
                                                         })
                                                         .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByRoleType(List<String> roleTypes) {
        return (root, query, builder) -> builder.or(roleTypes
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("roleType"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByRoleName(List<String> roleNames) {
        return (root, query, builder) -> builder.or(roleNames
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("roleName"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByClassification(List<String> classifications) {
        return (root, query, builder) -> builder.or(classifications
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("classification"), value))
                                                        .toArray(Predicate[]::new));

    }

    public static Specification<RoleAssignmentEntity> searchByRoleCategories(List<String> roleCategories) {
        return (root, query, builder) -> builder.or(roleCategories
                                                        .stream()
                                                        .map(value -> builder.equal(root.get("roleCategory"), value))
                                                        .toArray(Predicate[]::new));

    }


}
