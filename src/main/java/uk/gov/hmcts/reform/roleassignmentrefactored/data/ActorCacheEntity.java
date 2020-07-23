
package uk.gov.hmcts.reform.roleassignmentrefactored.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignmentrefactored.util.JsonBConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "actor_cache_control")
public class ActorCacheEntity {

    @Id
    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Version
    @Column(name = "etag", nullable = false)
    private long etag;

    @Column(name = "json_response", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = JsonBConverter.class)
    private JsonNode roleAssignmentResponse;

}

