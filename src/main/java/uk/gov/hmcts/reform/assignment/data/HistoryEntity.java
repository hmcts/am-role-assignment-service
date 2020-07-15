
package uk.gov.hmcts.reform.assignment.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.assignment.util.JsonBConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@SuperBuilder()
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "role_assignment_history")
@IdClass(RoleAssignmentIdentity.class)
public class HistoryEntity extends RoleAssignmentEntity {

    @Id
    private String status;

    @Column(name = "process")
    private String process;

    @Column(name = "reference")
    private String reference;

    @Column(name = "log")
    private String log;

    @Column(name = "status_sequence", nullable = false)
    private int sequence;

    @Column(name = "notes", nullable = true, columnDefinition = "jsonb")
    @Convert(converter = JsonBConverter.class)
    private JsonNode notes;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private RequestEntity requestEntity;

}

