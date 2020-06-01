package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Long> {
}
