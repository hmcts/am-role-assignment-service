package uk.gov.hmcts.reform.roleassignment.data.rolerequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.JSONBConverter;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.ActorIdType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@SequenceGenerator(name = "request_id_seq", sequenceName = "request_id_seq", allocationSize = 1)
public class RoleAssignmentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "role_assignment_id", nullable = false)
    private Long roleAssignmentId;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "authenticated_user_id", nullable = false)
    private UUID authenticatedUserId;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "process")
    private String process;

    @Column(name = "reference")
    private String reference;

    @Column(name = "replace_existing")
    private Boolean replaceExisting;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "last_update_time", nullable = false)
    private LocalDateTime lastUpdateTime;
}
