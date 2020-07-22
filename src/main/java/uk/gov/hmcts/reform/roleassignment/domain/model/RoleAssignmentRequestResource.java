package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

/*    @JsonIgnore
    private String reference;

    @JsonIgnore
    private String process;*/

    @JsonIgnore
    private String assignerId;

    //@JsonIgnore
    //private String id;

    @JsonIgnore
    private String actorId;

    @JsonIgnore
    private String roleName;

    @JsonIgnore
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
            //this.process = roleAssignment.getProcess();
            //this.reference = roleAssignment.getReference();
            //this.id = roleAssignment.getId().toString();
            this.actorId = roleAssignment.getActorId().toString();
            this.roleName = roleAssignment.getRoleName();
            this.caseId = roleAssignment.getAttributes().get("caseId").asText();


        }
        this.assignerId = assignmentRequest.getRequest().getAssignerId().toString();
    }


}

