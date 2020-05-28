package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignment.data.rolerequest.RoleAssignmentRequest;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Getter
@Setter
@Entity
@NoArgsConstructor
@SequenceGenerator(name = "role_assignment_history_status_id_seq", sequenceName = "role_assignment_history_status_id_seq", allocationSize = 1)
public class RoleAssignmentHistoryStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_assignment_history_status_id_seq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "role_assignment_id", nullable = false)
    private Long roleAssignmentId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "log")
    private String log;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @ManyToOne
    private RoleAssignmentHistory roleAssignmentHistory;


}
