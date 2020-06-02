
package uk.gov.hmcts.reform.roleassignment.data.rolerequest;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleAssignmentRequestRepository extends CrudRepository<RoleAssignmentRequestEntity, Long> {
}

