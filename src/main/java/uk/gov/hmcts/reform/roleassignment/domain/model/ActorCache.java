package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;

import java.util.Set;
import java.util.UUID;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public  class ActorCache {

    private UUID actorId;
    private long etag;
    private Set<RoleAssignmentEntity> roleAssignments;
}
