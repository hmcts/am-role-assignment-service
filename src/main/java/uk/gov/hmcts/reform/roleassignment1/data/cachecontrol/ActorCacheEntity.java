
package uk.gov.hmcts.reform.roleassignment1.data.cachecontrol;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment1.util.JsonBConverter;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "actor_cache_control")
public class ActorCacheEntity implements Serializable {

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

