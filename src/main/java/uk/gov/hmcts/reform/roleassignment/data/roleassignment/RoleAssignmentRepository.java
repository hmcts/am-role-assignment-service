
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, UUID> {

    Set<RoleAssignmentEntity> findByActorId(UUID actorId);
    Set<RoleAssignmentEntity> findByActorIdAndRoleType(UUID actorId, String roleType);

    void deleteByActorId(UUID actorId);

    //select ra.* from role_assignment ra where ra.actor_id  = '123e4567-e89b-42d3-a456-556642445612' and ra.attributes ->> 'caseId' = '1234567890123456' ;
    @Query(value = "select ra.* from role_assignment ra where ra.actor_id  = :actorId and " +
                   "role_type = :roleType and ra.attributes ->> 'caseId' = :caseId ;", nativeQuery = true)
    Set<RoleAssignmentEntity> findByActorIdAndCaseId(String actorId, String caseId, String roleType);

    @Query(value = "select ra.* from role_assignment ra where " +
                   "role_type = :roleType and ra.attributes ->> 'caseId' = :caseId ;", nativeQuery = true)
    Set<RoleAssignmentEntity> getAssignmentByCaseId(String caseId, String roleType);

}

