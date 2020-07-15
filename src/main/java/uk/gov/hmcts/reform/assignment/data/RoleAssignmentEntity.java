
package uk.gov.hmcts.reform.assignment.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import uk.gov.hmcts.reform.assignment.util.JsonBConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@SuperBuilder()
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "role_assignment")

public class RoleAssignmentEntity {

    @Id
    private UUID id;

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

    @Column(name = "role_category")
    private String roleCategory;

    @Column(name = "read_only", nullable = false)
    private boolean readOnly;

    @Column(name = "begin_time")
    private LocalDateTime beginTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = JsonBConverter.class)
    private JsonNode attributes;

}

