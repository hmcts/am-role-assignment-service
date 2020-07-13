package uk.gov.hmcts.reform.assignment.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Validated
public class RoleAssignmentRequestResource extends RepresentationModel<RoleAssignmentRequestResource> {

    @JsonProperty("roleAssignmentResponse")
    private AssignmentRequest roleAssignmentRequest;


    public RoleAssignmentRequestResource(AssignmentRequest roleAssignmentRequest) {
        this.roleAssignmentRequest = roleAssignmentRequest;
        //add(linkTo(methodOn(CreateAssignmentController.class).getRoleAssignmentByActorId("")).withRel("binary"))
    }

    public void addLinks(UUID documentId) {
        //add(linkTo(methodOn(CreateAssignmentController.class).getRoleAssignmentByActorId("")).withRel("binary"));
    }


}

