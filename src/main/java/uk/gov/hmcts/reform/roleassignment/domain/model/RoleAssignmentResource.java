package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.roleassignment.controller.endpoints.GetAssignmentController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Validated
@Slf4j
@JsonIgnoreProperties(value = { "links" })
public class RoleAssignmentResource extends RepresentationModel<RoleAssignmentResource> {

    @JsonProperty("roleAssignmentResponse")
    private List<? extends Assignment> roleAssignmentResponse;


    public RoleAssignmentResource(List<Assignment> roleAssignmentResponse, String actorId) {
        this.roleAssignmentResponse = roleAssignmentResponse;
        try {
            add(WebMvcLinkBuilder.linkTo(methodOn(GetAssignmentController.class).retrieveRoleAssignmentsByActorId(
                "",
                "",
                actorId)).withRel("binary"));
        } catch (Exception e) {
            log.error("context", e);
        }
    }

    public RoleAssignmentResource(List<? extends Assignment> roleAssignmentResponse) {
        this.roleAssignmentResponse = roleAssignmentResponse;
    }
}
