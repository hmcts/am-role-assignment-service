package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdTypeEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@Entity(name = "roleassignmenthistory")
@SequenceGenerator(name = "role_assignment_id_seq", sequenceName = "role_assignment_id_seq", allocationSize = 1)
public class RoleAssignmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_assignment_id_seq")
    @Column(name = "roleassignmentid", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "actoridtype", nullable = false)
    private ActorIdTypeEnum actorIdTypeEnum;

    @Column(name = "actorid", nullable = false)
    private UUID actorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "roletypeid", nullable = false)
    private RoleType roleType;

    @Column(name = "rolename", nullable = false)
    private String roleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "classificationid", nullable = false)
    private Classification classification;

    @Enumerated(EnumType.STRING)
    @Column(name = "granttypeid", nullable = false)
    private GrantType grantType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "readonly")
    private boolean readOnly;

    @Column(name = "begintime")
    private LocalDateTime beginTime;

    @Column(name = "endtime")
    private LocalDateTime endTime;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "lastupdated", nullable = false)
    private LocalDateTime lastUpdateTime;

    @Column(name = "attributes", nullable = false, columnDefinition = "jsonb")
    @Convert(converter = JsonBConverter.class)
    private JsonNode attributes;

    @OneToMany(cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        mappedBy = "roleAssignmentHistory")
    private Set<RoleAssignmentHistoryStatus> roleAssignmentHistoryStatus;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "request_id")
//    private RoleAssignmentRequest roleAssignmentRequest;

//    @Column(name = "request_id", nullable = false)
//    private Long requestId;
}
