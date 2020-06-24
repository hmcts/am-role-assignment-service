
package uk.gov.hmcts.reform.roleassignment.data.cache;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.roleassignment.util.JsonBConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "actor_cache_control")
public class CacheControlEntity implements Serializable {

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

