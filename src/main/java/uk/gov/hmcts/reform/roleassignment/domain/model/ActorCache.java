package uk.gov.hmcts.reform.roleassignment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.data.RoleAssignmentEntity;

import java.time.LocalDateTime;
import java.util.Set;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public  class ActorCache {

    private String actorId;
    private long etag;

}
