
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.hmcts.reform.roleassignment.util.JsonBConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.EmbeddedId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@Entity(name = "role_assignment_history")
public class HistoryEntity implements Serializable {

    @EmbeddedId
    private RoleAssignmentIdentity roleAssignmentIdentity;

    @Column(name = "actor_id_type", nullable = false)
    private String actorIdType;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "role_type", nullable = false)
    private String roleType;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "classification", nullable = false)
    private String classification;

    @Column(name = "grant_type", nullable = false)
    private String grantType;

    @Column(name = "read_only")
    private boolean readOnly;

    @Column(name = "begin_time")
    private LocalDateTime beginTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "log")
    private String log;

    @Column(name = "status_sequence", nullable = false)
    private int sequence;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdateTime;

    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = JsonBConverter.class)
    private JsonNode attributes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private RequestEntity requestEntity;

    //getter method to retrieve the parent id in the child entity
    public UUID getRequestId() {
        return requestEntity.getId();
    }

}

