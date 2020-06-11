
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@Entity(name = "role_assignment_request")
public class RequestEntity implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "authenticated_user_id", nullable = false)
    private UUID authenticatedUserId;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(name = "request_type", nullable = false)
    private String requestType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "process")
    private String process;

    @Column(name = "reference")
    private String reference;

    @Column(name = "replace_existing")
    private Boolean replaceExisting;

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

    @OneToMany(cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        mappedBy = "requestEntity")
    private Set<HistoryEntity> historyEntities;


}

