
package uk.gov.hmcts.reform.roleassignment.data;

import org.springframework.data.repository.CrudRepository;

import java.sql.SQLException;

public interface ActorCacheRepository extends CrudRepository<ActorCacheEntity, String> {

    ActorCacheEntity findByActorId(String actorId) throws SQLException;
}

