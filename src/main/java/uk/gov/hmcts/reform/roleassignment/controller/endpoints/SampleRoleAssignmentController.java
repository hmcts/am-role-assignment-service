package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentHistoryStatusEntity;
import uk.gov.hmcts.reform.roleassignment.data.rolerequest.RoleAssignmentRequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.rolerequest.RoleAssignmentRequestStatusEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentHistoryEntity;
import uk.gov.hmcts.reform.roleassignment.data.rolerequest.RoleAssignmentRequestEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.UUID;

@RestController
public class SampleRoleAssignmentController {

    private RoleAssignmentRequestRepository roleAssignmentRequestRepository;
    ObjectMapper objectMapper;

    public SampleRoleAssignmentController(RoleAssignmentRequestRepository roleAssignmentRequestRepository) {
        this.roleAssignmentRequestRepository = roleAssignmentRequestRepository;
    }


    @GetMapping("/insertEntity")
    public String insertEntity() {

        convertIntoObject();

        return "Success";


    }

    private void convertIntoObject() {
        try {
            objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat());
            RoleAssignment model;
            try (InputStream input = SampleRoleAssignmentController.class.getClassLoader().getResourceAsStream(
                "roleassignmenthistroy.json")) {

                model = objectMapper.readValue(input, RoleAssignment.class);

            }
            RoleAssignmentHistoryEntity roleAssignmentHistoryEntity = convertIntoEntity(model);
            roleAssignmentHistoryEntity.setRoleAssignmentHistoryStatusEntities(new HashSet<RoleAssignmentHistoryStatusEntity>());
            buildRoleAssignmentHistoryStatus(roleAssignmentHistoryEntity);

            //prepare request
            RoleAssignmentRequestEntity roleAssignmentRequestEntity = buildRoleAssignmentRequest(
                roleAssignmentHistoryEntity);
            roleAssignmentRequestEntity.setRoleAssignmentRequestStatusEntities(new HashSet<RoleAssignmentRequestStatusEntity>());
            buildRoleAssignmentRequestStatusEntity(roleAssignmentRequestEntity);


            roleAssignmentRequestRepository.save(roleAssignmentRequestEntity);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RoleAssignmentHistoryEntity convertIntoEntity(RoleAssignment model) {
        return RoleAssignmentHistoryEntity.builder().actorId(model.getActorId())
            .actorIdType(model.getActorIdType().toString())
            .attributes(convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification().toString())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType().toString())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType().toString())
            .status(model.getStatus().toString())
            .readOnly(Boolean.TRUE)
            .build();
    }

    private void buildRoleAssignmentHistoryStatus(RoleAssignmentHistoryEntity roleAssignmentHistoryEntity) {
        RoleAssignmentHistoryStatusEntity roleAssignmentHistoryStatusEntity = RoleAssignmentHistoryStatusEntity.builder().roleAssignmentHistoryEntity(
            roleAssignmentHistoryEntity)
            .log("professional drools rule")
            .status(Status.CREATED.toString())
            .sequence(102)
            .build();
        roleAssignmentHistoryEntity.getRoleAssignmentHistoryStatusEntities().add(roleAssignmentHistoryStatusEntity);
    }

    private RoleAssignmentRequestEntity buildRoleAssignmentRequest(RoleAssignmentHistoryEntity roleAssignmentHistoryEntity) {
        RoleAssignmentRequestEntity roleAssignmentRequestEntity = RoleAssignmentRequestEntity.builder()
            .correlationId("request1")
            .status(Status.CREATED.toString())
            .process("businessProcess1")
            .reference("abc-3434242")
            .authenticatedUserId(UUID.randomUUID())
            .clientId("sdsd")
            .requesterId(UUID.randomUUID())
            .replaceExisting(Boolean.FALSE)
            .requestType(RequestType.CREATE.toString())
            .build();
        roleAssignmentRequestEntity.setRoleAssignmentHistoryEntities(new HashSet<RoleAssignmentHistoryEntity>());
        roleAssignmentRequestEntity.getRoleAssignmentHistoryEntities().add(roleAssignmentHistoryEntity);
        roleAssignmentHistoryEntity.setRoleAssignmentRequestEntity(roleAssignmentRequestEntity);
        return roleAssignmentRequestEntity;


    }

    private void buildRoleAssignmentRequestStatusEntity(RoleAssignmentRequestEntity roleAssignmentRequestEntity) {
        RoleAssignmentRequestStatusEntity roleAssignmentRequestStatusEntity = RoleAssignmentRequestStatusEntity.builder()
            .log("rools")
            .sequence(110)
            .status(Status.CREATED.toString())
            .roleAssignmentRequestEntity(roleAssignmentRequestEntity)
            .build();

        roleAssignmentRequestEntity.getRoleAssignmentRequestStatusEntities().add(roleAssignmentRequestStatusEntity);

    }


    public JsonNode convertValueJsonNode(Object from) {
        return objectMapper.convertValue(from, JsonNode.class);
    }
}
