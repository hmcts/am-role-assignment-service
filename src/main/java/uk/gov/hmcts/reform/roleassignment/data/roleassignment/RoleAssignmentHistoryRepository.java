
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleAssignmentHistoryRepository extends CrudRepository<RoleAssignmentHistory, Long> {
}

