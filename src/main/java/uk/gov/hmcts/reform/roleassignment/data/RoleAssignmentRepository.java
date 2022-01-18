
package uk.gov.hmcts.reform.roleassignment.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, UUID>,
    JpaSpecificationExecutor<RoleAssignmentEntity> {

    Set<RoleAssignmentEntity> findByActorId(String actorId) throws SQLException;

    void deleteByActorId(String actorId);


}

