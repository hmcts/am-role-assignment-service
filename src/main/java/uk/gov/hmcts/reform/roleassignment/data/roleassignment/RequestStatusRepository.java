
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RequestStatusRepository extends CrudRepository<RequestStatusEntity, UUID> {
}

