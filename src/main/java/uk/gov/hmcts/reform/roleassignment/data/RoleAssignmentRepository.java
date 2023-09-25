
package uk.gov.hmcts.reform.roleassignment.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignmentEntity, UUID>,
    JpaSpecificationExecutor<RoleAssignmentEntity> {

    Set<RoleAssignmentEntity> findByActorId(String actorId) throws SQLException;

    void deleteByActorId(String actorId);

    @Query(
        value = "SELECT"
            + "   jsonb_extract_path_text(ATTRIBUTES,'jurisdiction') AS jurisdiction,"
            + "   role_category,"
            + "   count(DISTINCT actor_id) AS count"
            + "  FROM role_assignment"
            + " WHERE role_type='ORGANISATION'"
            + " GROUP BY jsonb_extract_path_text(attributes ,'jurisdiction'), role_category"
            + " ORDER BY jsonb_extract_path_text(attributes ,'jurisdiction') asc, role_category asc;",
        nativeQuery = true)
    List<JurisdictionRoleCategoryAndCount> getOrgUserCountByJurisdiction() throws SQLException;

    @Query(
        value = "SELECT"
            + "   jsonb_extract_path_text(ATTRIBUTES,'jurisdiction') AS jurisdiction,"
            + "   role_category,"
            + "   role_name,"
            + "   count(DISTINCT actor_id) AS count"
            + "  FROM role_assignment"
            + " WHERE role_type='ORGANISATION'"
            + " GROUP BY jsonb_extract_path_text(attributes ,'jurisdiction'), role_category, role_name"
            + " ORDER BY jsonb_extract_path_text(attributes ,'jurisdiction') asc, role_category ASC, role_name ASC;",
        nativeQuery = true)
    List<JurisdictionRoleCategoryNameAndCount> getOrgUserCountByJurisdictionAndRoleName() throws SQLException;

    @JsonPropertyOrder({ "jurisdiction", "count"})
    public interface JurisdictionRoleCategoryAndCount {
        String getJurisdiction();

        String getRoleCategory();

        BigInteger getCount();
    }

    @JsonPropertyOrder({ "jurisdiction", "role_category", "role_name", "count"})
    public interface JurisdictionRoleCategoryNameAndCount {
        String getJurisdiction();

        String getRoleCategory();

        String getRoleName();

        BigInteger getCount();
    }


}

