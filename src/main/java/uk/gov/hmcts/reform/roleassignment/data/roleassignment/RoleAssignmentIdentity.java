
package uk.gov.hmcts.reform.roleassignment.data.roleassignment;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RoleAssignmentIdentity implements Serializable {

    private UUID id;

    private String status;
}

