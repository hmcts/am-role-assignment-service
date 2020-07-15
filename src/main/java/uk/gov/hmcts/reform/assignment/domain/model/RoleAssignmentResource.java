package uk.gov.hmcts.reform.assignment.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.assignment.controller.endpoints.GetAssignmentController;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Validated
public class RoleAssignmentResource extends RepresentationModel<RoleAssignmentResource> {

    @JsonProperty("roleAssignmentResponse")
    private List<RoleAssignment> roleAssignmentResponse;


    public RoleAssignmentResource(List<RoleAssignment> roleAssignmentResponse, UUID actorId) {
        this.roleAssignmentResponse = roleAssignmentResponse;
        try {
            add(WebMvcLinkBuilder.linkTo(methodOn(GetAssignmentController.class).retrieveRoleAssignmentsByActorId(
                "",
                actorId.toString())).withRel("binary"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
