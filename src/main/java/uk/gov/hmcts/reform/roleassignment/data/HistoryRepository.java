
package uk.gov.hmcts.reform.roleassignment.data;

import org.springframework.data.repository.CrudRepository;

public interface HistoryRepository extends CrudRepository<HistoryEntity, RoleAssignmentIdentity> {

}

