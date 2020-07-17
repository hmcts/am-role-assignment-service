
package uk.gov.hmcts.reform.roleassignment.data;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, UUID> {

    Set<RoleAssignmentEntity> findByActorId(UUID actorId);

    Set<RoleAssignmentEntity> findByActorIdAndRoleType(UUID actorId, String roleType);

    void deleteByActorId(UUID actorId);

    @Query(value = "select ra.* from role_assignment ra where ra.actor_id  = :actorId "
                   + "and ra.attributes ->> 'caseId' = :caseId "
                   + "and ra.role_type = :roleType", nativeQuery = true)
    Set<RoleAssignmentEntity> findByActorIdAndCaseId(String actorId, String caseId, String roleType);

    @Query(value = "select ra.* from role_assignment ra where "
                   + "ra.role_type = :roleType and ra.attributes ->> 'caseId' = :caseId ;", nativeQuery = true)
    Set<RoleAssignmentEntity> getAssignmentByCaseId(String caseId, String roleType);

}

