
package uk.gov.hmcts.reform.roleassignment.data;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;
import uk.gov.hmcts.reform.roleassignment.util.JsonBConverter;
import uk.gov.hmcts.reform.roleassignment.util.StringListConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "role_assignment")

public class RoleAssignmentEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "actor_id_type", nullable = false)
    private String actorIdType;

    @Column(name = "actor_id", nullable = false)
    private String actorId;

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

    @Column(name = "authorisations")
    //@ElementCollection(targetClass = String.class)
    @Convert(converter = StringListConverter.class)
    private List<String> authorisations;

    @Builder.Default
    @Transient
    private boolean isNewFlag = true;

    @Override
    public boolean isNew() {
        return isNewFlag;
    }

    /*public List<String> getAuthorisations() {
        return Arrays.asList(authorisations);
    }

    public void setAuthorisations(List<String> authorisations) {
        this.authorisations = authorisations.toArray(String[]::new);
    }

    public RoleAssignmentEntity setAuthorisationsBuilder(List<String> authorisations) {
        this.authorisations = authorisations.toArray(String[]::new);
        return this;
    }*/

}

