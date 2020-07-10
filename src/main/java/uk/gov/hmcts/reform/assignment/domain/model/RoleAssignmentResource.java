package uk.gov.hmcts.reform.assignment.domain.model;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.assignment.controller.endpoints.GetAssignmentController;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Validated
public class RoleAssignmentResource extends RepresentationModel<RoleAssignmentResource> {

    @JsonProperty("roleAssignmentResponse")
    private List<RoleAssignment> roleAssignmentResponse;


    public RoleAssignmentResource(List<RoleAssignment> roleAssignmentResponse, UUID actorId) throws Exception {
        this.roleAssignmentResponse = roleAssignmentResponse;
        add(WebMvcLinkBuilder.linkTo(methodOn(GetAssignmentController.class).retrieveRoleAssignmentsByActorId(
            "",
            actorId.toString())).withRel("binary"));
    }
}
