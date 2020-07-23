
package uk.gov.hmcts.reform.roleassignmentrefactored.data;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ActorCacheRepository extends CrudRepository<ActorCacheEntity, UUID> {

    ActorCacheEntity findByActorId(UUID actorId);
}

