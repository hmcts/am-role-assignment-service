package uk.gov.hmcts.reform.assignment.controller.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.assignment.controller.advice.exception.ServiceException;
import uk.gov.hmcts.reform.assignment.data.roleassignment.HistoryEntity;
import uk.gov.hmcts.reform.assignment.data.roleassignment.RequestEntity;
import uk.gov.hmcts.reform.assignment.data.roleassignment.RequestRepository;
import uk.gov.hmcts.reform.assignment.domain.model.AssignmentRequest;
import uk.gov.hmcts.reform.assignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.assignment.domain.model.enums.RequestType;
import uk.gov.hmcts.reform.assignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.assignment.domain.service.common.ValidationModelService;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.UUID;

@RestController
public class SampleRoleAssignmentController {

    private RequestRepository requestRepository;
    ObjectMapper objectMapper;

    private ValidationModelService validationModelService;

    public SampleRoleAssignmentController(RequestRepository requestRepository,
                                          ValidationModelService validationModelService) {
        this.requestRepository = requestRepository;
        this.validationModelService = validationModelService;
    }


    @GetMapping("/insertEntity")
    public String insertEntity() {

        convertIntoObject();

        return "Success";


    }

    @PostMapping("/fireRoleExecution")
    public AssignmentRequest testRue(@RequestBody AssignmentRequest assignmentRequest) {

        validationModelService.validateRequest(assignmentRequest);

        return assignmentRequest;
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
            //prepare request
            RequestEntity requestEntity = buildRoleAssignmentRequest(
                historyEntity);

            RequestEntity request = requestRepository.save(requestEntity);

            updateStatusOfRequest(request);


        } catch (Exception e) {
            throw new ServiceException("Service Exception", e);
        }
    }

    private void updateStatusOfRequest(RequestEntity requestEntity) {
        requestEntity.setStatus(Status.APPROVED.toString());
        requestRepository.save(requestEntity);
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
            .log("professional drools rule")
            .sequence(102)
            .build();
    }

    private RequestEntity buildRoleAssignmentRequest(HistoryEntity historyEntity) {
        RequestEntity requestEntity = RequestEntity.builder()
            .correlationId("request1")
            .status(Status.CREATED.toString())
            .process("businessProcess1")
            .reference("abc-3434242")
            .authenticatedUserId(UUID.randomUUID())
            .clientId("sdsd")
            .assignerId(UUID.randomUUID())
            .replaceExisting(Boolean.FALSE)
            .requestType(RequestType.CREATE.toString())
            .log("professional drools rule")
            .build();
        requestEntity.setHistoryEntities(new HashSet<>());
        requestEntity.getHistoryEntities().add(historyEntity);
        historyEntity.setRequestEntity(requestEntity);
        return requestEntity;


    }


    public JsonNode convertValueJsonNode(Object from) {
        return objectMapper.convertValue(from, JsonNode.class);
    }
}
