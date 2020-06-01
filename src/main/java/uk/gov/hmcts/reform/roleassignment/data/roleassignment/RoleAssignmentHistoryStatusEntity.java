
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@Entity(name = "role_assignment_history_status")
public class RoleAssignmentHistoryStatusEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private UUID id;


    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "log")
    private String log;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_assignment_id", nullable = false)
    private RoleAssignmentHistoryEntity roleAssignmentHistoryEntity;

}

