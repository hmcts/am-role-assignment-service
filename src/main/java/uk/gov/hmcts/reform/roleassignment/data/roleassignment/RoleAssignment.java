package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class RoleAssignment {
    @Id
    @Column(name = "role_assignment_id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_id_type", nullable = false)
    private ActorIdType actorIdType;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private RoleType roleType;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false)
    private Classification classification;

    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type", nullable = false)
    private GrantType grantType;

    @Column(name = "readOnly", nullable = false)
    private boolean readOnly;

    @Column(name = "begin_time")
    private LocalDateTime beginTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "attributes", nullable = false)
    @Convert(converter = JsonBConverter.class)
    private Map<String, String> attributes;
}
