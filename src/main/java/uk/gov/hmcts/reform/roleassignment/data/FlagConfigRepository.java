package uk.gov.hmcts.reform.roleassignment.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlagConfigRepository extends CrudRepository<FlagConfig, Long> {

    @Query("select fc from flag_config as fc"
        + " where fc.flagName=?1 "
        + " and upper(fc.env) = upper(?2)")
    FlagConfig getStatusByParams(String flagName, String envName);

}
