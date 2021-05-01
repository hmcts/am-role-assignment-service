
package uk.gov.hmcts.reform.roleassignment.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;
import uk.gov.hmcts.reform.roleassignment.util.JsonBConverter;

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
public class ActorCacheEntity implements Persistable<String> {

    @Id
    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @Version
    @Column(name = "etag", nullable = false)
    private long etag;

    @Column(name = "json_response", nullable = true, columnDefinition = "jsonb")
    @Convert(converter = JsonBConverter.class)
    private JsonNode roleAssignmentResponse;

    @Override
    public String getId() {
        return actorId;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}

