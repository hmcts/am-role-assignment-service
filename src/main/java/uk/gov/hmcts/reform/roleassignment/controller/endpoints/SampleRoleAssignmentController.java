package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryStatusEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RequestStatusEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.UUID;

@RestController
public class SampleRoleAssignmentController {

    private RequestRepository requestRepository;
    ObjectMapper objectMapper;

    public SampleRoleAssignmentController(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
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
            HistoryEntity historyEntity = convertIntoEntity(model);
            historyEntity.setRoleAssignmentHistoryStatusEntities(new HashSet<HistoryStatusEntity>());
            buildRoleAssignmentHistoryStatus(historyEntity);

            //prepare request
            RequestEntity requestEntity = buildRoleAssignmentRequest(
                historyEntity);
            requestEntity.setRoleAssignmentRequestStatusEntities(new HashSet<RequestStatusEntity>());
            buildRoleAssignmentRequestStatusEntity(requestEntity);


            requestRepository.save(requestEntity);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HistoryEntity convertIntoEntity(RoleAssignment model) {
        return HistoryEntity.builder().actorId(model.getActorId())
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

    private void buildRoleAssignmentHistoryStatus(HistoryEntity historyEntity) {
        HistoryStatusEntity historyStatusEntity = HistoryStatusEntity.builder().historyEntity(
            historyEntity)
            .log("professional drools rule")
            .status(Status.CREATED.toString())
            .sequence(102)
            .build();
        historyEntity.getRoleAssignmentHistoryStatusEntities().add(historyStatusEntity);
    }

    private RequestEntity buildRoleAssignmentRequest(HistoryEntity historyEntity) {
        RequestEntity requestEntity = RequestEntity.builder()
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
        requestEntity.setRoleAssignmentHistoryEntities(new HashSet<HistoryEntity>());
        requestEntity.getRoleAssignmentHistoryEntities().add(historyEntity);
        historyEntity.setRequestEntity(requestEntity);
        return requestEntity;


    }

    private void buildRoleAssignmentRequestStatusEntity(RequestEntity requestEntity) {
        RequestStatusEntity requestStatusEntity = RequestStatusEntity.builder()
            .log("rools")
            .sequence(110)
            .status(Status.CREATED.toString())
            .requestEntity(requestEntity)
            .build();

        requestEntity.getRoleAssignmentRequestStatusEntities().add(requestStatusEntity);

    }


    public JsonNode convertValueJsonNode(Object from) {
        return objectMapper.convertValue(from, JsonNode.class);
    }
}
