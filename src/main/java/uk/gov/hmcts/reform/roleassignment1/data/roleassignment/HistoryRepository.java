
package uk.gov.hmcts.reform.roleassignment1.data.roleassignment;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface HistoryRepository extends CrudRepository<HistoryEntity, RoleAssignmentIdentity> {

    @Query("select p from role_assignment_history as p "
        + " where upper(p.process) = upper(?1) and  upper(p.reference) = upper(?2)"
        + "  and p.status= ?3 "
        + "and p.id IN (select id from role_assignment)")
    Set<HistoryEntity> findByReference(String process, String reference, String status);


}

