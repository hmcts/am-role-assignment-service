
package uk.gov.hmcts.reform.roleassignment.data.cache;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CacheControlRepository extends CrudRepository<CacheControlEntity, UUID> {

    CacheControlEntity findByActorId(UUID actorId);
}

