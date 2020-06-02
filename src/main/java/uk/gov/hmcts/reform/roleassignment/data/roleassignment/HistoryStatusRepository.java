
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface HistoryStatusRepository extends CrudRepository<HistoryEntity, UUID> {
}

