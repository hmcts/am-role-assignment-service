
package uk.gov.hmcts.reform.roleassignment.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, UUID>,
    JpaSpecificationExecutor<RoleAssignmentEntity> {

    Set<RoleAssignmentEntity> findByActorId(String actorId);

    void deleteByActorId(String actorId);

    Set<RoleAssignmentEntity> findByProcessIgnoreCaseAndReferenceIgnoreCase(String process, String reference);
}

