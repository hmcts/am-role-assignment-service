package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Builder(toBuilder = true)
@Getter
@Setter
@Entity(name = "roleassignmenthistorystatus")
@SequenceGenerator(name = "roleassignmenthistorystatusid_sequence", sequenceName = "roleassignmenthistorystatusid_sequence", allocationSize = 1)
public class RoleAssignmentHistoryStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roleassignmenthistorystatusid_sequence")
    @Column(name = "roleassignmenthistorystatusid", nullable = false)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "log")
    private String log;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roleassignmentid", nullable = false)
    private RoleAssignmentHistory roleAssignmentHistory;

//    @ManyToOne
//    @JoinColumn(name="roleassignmentrequestid", nullable=false)
//    private RoleAssignmentRequest roleAssignmentRequest;
}
