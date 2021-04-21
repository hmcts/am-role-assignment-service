package uk.gov.hmcts.reform.roleassignment.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlagConfigRepository extends CrudRepository<FlagConfig, Long> {

    @Query("select * from flag_config "
        + " where flag_name=?1 "
        + " and upper(env) = upper(?2) and upper(service_name) = upper(?3) ")
    FlagConfig getStatusByParams(String flagName, String envName, String serviceName);
}
