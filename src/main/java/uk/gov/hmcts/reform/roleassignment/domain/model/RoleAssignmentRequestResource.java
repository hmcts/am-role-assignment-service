package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Validated
public class RoleAssignmentRequestResource extends RepresentationModel<RoleAssignmentRequestResource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAssignmentRequestResource.class);

    @JsonProperty("roleAssignmentResponse")
    private AssignmentRequest roleAssignmentRequest;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("process")
    private String process;

    @JsonProperty("assignerId")
    private String assignerId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("actorId")
    private String actorId;

    @JsonProperty("roleName")
    private String roleName;

    @JsonProperty("caseId")
    private String caseId;




    public RoleAssignmentRequestResource(@NonNull AssignmentRequest roleAssignmentRequest) {
        this.roleAssignmentRequest = roleAssignmentRequest;
        copyProperties(roleAssignmentRequest);
        //add(linkTo(methodOn(CreateAssignmentController.class).getRoleAssignmentByActorId("")).withRel("binary"))
    }

    public void addLinks(UUID documentId) {
        LOGGER.info(" add links for document...{}", documentId);
    }

    private void copyProperties(AssignmentRequest assignmentRequest) {
        for (RoleAssignment roleAssignment : assignmentRequest.getRequestedRoles()) {
            this.process = roleAssignment.getProcess();
            this.reference = roleAssignment.getReference();
            this.id = roleAssignment.getId().toString();
            this.actorId = roleAssignment.getActorId().toString();
            this.roleName = roleAssignment.getRoleName();
            this.caseId = roleAssignment.getAttributes().get("caseId").asText();


        }
        this.assignerId = assignmentRequest.getRequest().getAssignerId().toString();
    }


}

