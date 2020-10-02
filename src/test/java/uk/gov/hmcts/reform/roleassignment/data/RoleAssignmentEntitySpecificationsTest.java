package uk.gov.hmcts.reform.roleassignment.data;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RoleAssignmentEntitySpecificationsTest {

    @Mock
    Specification<RoleAssignmentEntity> mockSpec;
    @Mock
    Root<RoleAssignmentEntity> root;
    @Mock
    CriteriaQuery<RoleAssignmentEntity> query;
    @Mock
    CriteriaBuilder builder;

    @Mock
    Predicate predicate;


    @Before
    public void setUp() {
        when(mockSpec.toPredicate(root, query, builder)).thenReturn(predicate);


    }

    @Test
    public void shouldReturnPredicate_WhileSearchByActorIds() {
        List<String> actorId = Arrays.asList(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );

        Specification<RoleAssignmentEntity> spec = RoleAssignmentEntitySpecifications.searchByActorIds(actorId);
        spec = spec.and(mockSpec);
        assertThat(spec).isNotNull();
        MatcherAssert.assertThat(spec.toPredicate(root, query, builder), is(predicate));


    }

    @Test
    public void shouldNotReturnPredicate_WhileSearchByActorIdsWithoutMock() {
        List<String> actorId = Arrays.asList(
            "123e4567-e89b-42d3-a456-556642445678",
            "4dc7dd3c-3fb5-4611-bbde-5101a97681e1"
        );

        Specification<RoleAssignmentEntity> spec = RoleAssignmentEntitySpecifications.searchByActorIds(actorId);
        assertThat(spec).isNotNull();
        MatcherAssert.assertThat(spec.toPredicate(root, query, builder), is(nullValue()));


    }

    @Test
    public void shouldReturnPredicate_WhileSearchByValidDate() {


        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByValidDate(
            LocalDateTime.now());
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(predicate));

    }

    @Test
    public void shouldNotReturnPredicate_WhileSearchByValidDateWithoutMock() {


        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByValidDate(
            LocalDateTime.now());

        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(nullValue()));

    }

    @Test
    public void shouldReturnPredicate_WhileSearchByRoleName() {
        List<String> roleNmaes = Arrays.asList("judge", "senior judge");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByRoleName(
            roleNmaes);
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(predicate));

    }

    @Test
    public void shouldNotReturnPredicate_WhileSearchByRoleNameWithoutMock() {
        List<String> roleNmaes = Arrays.asList("judge", "senior judge");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByRoleName(
            roleNmaes);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(nullValue()));

    }

    @Test
    public void shouldReturnPredicate_WhileSearchByRoleType() {
        List<String> roleTypes = Arrays.asList("CASE", "ORGANISATION");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByRoleType(
            roleTypes);
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(predicate));

    }

    @Test
    public void shouldNotReturnPredicate_WhileSearchByRoleTypeWithoutMock() {
        List<String> roleTypes = Arrays.asList("CASE", "ORGANISATION");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByRoleType(
            roleTypes);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(nullValue()));

    }

    @Test
    public void shouldReturnPredicate_WhileSearchByClassification() {
        List<String> classifications = Arrays.asList("PUBLIC", "PRIVATE");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByClassification(
            classifications);
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(predicate));

    }

    @Test
    public void shouldNotReturnPredicate_WhileSearchByClassificationWithoutMock() {
        List<String> classifications = Arrays.asList("PUBLIC", "PRIVATE");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByClassification(
            classifications);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(nullValue()));

    }

    @Test
    public void shouldReturnPredicate_WhileSearchByGrantType() {
        List<String> grantTypes = Arrays.asList("SPECIFIC", "STANDARD");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByGrantType(
            grantTypes);
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(predicate));

    }

    @Test
    public void shouldNotReturnPredicate_WhileSearchByGrantTypeWithoutMock() {
        List<String> grantTypes = Arrays.asList("SPECIFIC", "STANDARD");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByGrantType(
            grantTypes);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(nullValue()));

    }

    @Test
    public void shouldReturnPredicate_WhileSearchByRoleCategories() {
        List<String> roleCategories = Arrays.asList("JUDICIAL");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByRoleCategories(
            roleCategories);
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(predicate));

    }

    @Test
    public void shouldNotReturnPredicate_WhileSearchByRoleCategoriesWithoutMock() {
        List<String> roleCategories = Arrays.asList("JUDICIAL");
        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByRoleCategories(
            roleCategories);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(nullValue()));

    }

    @Test
    public void shouldReturnPredicate_WhileSearchByAttributes() {
        Map<String, List<String>> attributes = new HashMap<>();
        List<String> regions = Arrays.asList("London", "JAPAN");
        List<String> contractTypes = Arrays.asList("SALARIED", "Non SALARIED");
        attributes.put("region", regions);
        attributes.put("contractType", contractTypes);

        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByAttributes(
            attributes);
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(predicate));

    }

    @Test
    public void shouldNotReturnPredicate_WhileSearchByAttributesWithoutMock() {
        Map<String, List<String>> attributes = new HashMap<>();
        List<String> regions = Arrays.asList("London", "JAPAN");
        List<String> contractTypes = Arrays.asList("SALARIED", "Non SALARIED");
        attributes.put("region", regions);
        attributes.put("contractType", contractTypes);

        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByAttributes(
            attributes);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(nullValue()));

    }

    /*@Test
    public void shouldReturnPredicate_WhileSearchByAuthorisations() {
        List<String> authorisations = Arrays.asList("dev",
                                                    "tester"
        );

        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByAuthorisations(
            authorisations);
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(predicate));

    }*/

   /* @Test
    public void shouldNotReturnPredicate_WhileSearchByAuthorisationsWithoutMock() {
        List<String> authorisations = Arrays.asList("dev",
                                                    "tester"
        );

        Specification<RoleAssignmentEntity> specification = RoleAssignmentEntitySpecifications.searchByAuthorisations(
            authorisations);
        specification = specification.and(mockSpec);
        assertThat(specification).isNotNull();
        MatcherAssert.assertThat(specification.toPredicate(root, query, builder), is(nullValue()));

    }*/


}
