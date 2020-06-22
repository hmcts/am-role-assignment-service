
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface HistoryRepository extends CrudRepository<HistoryEntity, RoleAssignmentIdentity> {

    @Query("select p from role_assignment_history p where p.status= 'LIVE' and  p.process= process and  p.reference = reference ")
    Set<HistoryEntity> findByReference(String process, String reference, String status);
}

