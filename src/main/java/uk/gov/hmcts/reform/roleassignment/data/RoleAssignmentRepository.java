
package uk.gov.hmcts.reform.roleassignment.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, UUID>,
                                               JpaSpecificationExecutor<RoleAssignmentEntity> {

    Set<RoleAssignmentEntity> findByActorId(String actorId);

    Set<RoleAssignmentEntity> findByActorIdAndRoleTypeIgnoreCase(String actorId, String roleType);

    void deleteByActorId(String actorId);

    @Query(value = "select ra.* from role_assignment ra where ra.actor_id  = :actorId "
        + "and ra.attributes ->> 'caseId' = :caseId "
        + "and ra.role_type = UPPER(:roleType)", nativeQuery = true)
    Set<RoleAssignmentEntity> findByActorIdAndCaseId(String actorId, String caseId, String roleType);

    @Query(value = "select ra.* from role_assignment ra where "
        + "ra.role_type = UPPER(:roleType) and ra.attributes ->> 'caseId' = :caseId ;", nativeQuery = true)
    Set<RoleAssignmentEntity> getAssignmentByCaseId(String caseId, String roleType);

}

