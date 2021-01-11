package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.validation.annotation.Validated;


@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Validated
@Slf4j
@JsonIgnoreProperties(value = { "links" })
public class RoleAssignmentDeleteResource extends RepresentationModel<RoleAssignmentDeleteResource> {


    @JsonProperty("roleRequest")
    private Request roleRequest;

    public RoleAssignmentDeleteResource(Request roleRequest) {
        this.roleRequest = roleRequest;
    }


}
