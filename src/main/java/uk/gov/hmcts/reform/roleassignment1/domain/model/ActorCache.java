package uk.gov.hmcts.reform.roleassignment1.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment1.data.roleassignment.RoleAssignmentEntity;

import java.util.Set;
import java.util.UUID;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public  class ActorCache {

    public UUID actorId;
    public long etag;
    public Set<RoleAssignmentEntity> roleAssignments;
}
