package uk.gov.hmcts.reform.roleassignment.controller.endpoints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentHistoryStatus;
import uk.gov.hmcts.reform.roleassignment.domain.model.RoleAssignment;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentHistory;
import uk.gov.hmcts.reform.roleassignment.data.roleassignment.RoleAssignmentHistoryRepository;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashSet;

@RestController
public class SampleRoleAssignmentController {

    private RoleAssignmentHistoryRepository roleAssignmentRepository;
    ObjectMapper objectMapper;

    public SampleRoleAssignmentController(RoleAssignmentHistoryRepository roleAssignmentRepository) {

        this.roleAssignmentRepository = roleAssignmentRepository;
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
            RoleAssignmentHistory roleAssignmentHistory = convertIntoEntity(model);
            roleAssignmentHistory.setRoleAssignmentHistoryStatus(new HashSet<RoleAssignmentHistoryStatus>());
            buildRoleAssignmentHistoryStatus(roleAssignmentHistory);

            roleAssignmentRepository.save(roleAssignmentHistory);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RoleAssignmentHistory convertIntoEntity(RoleAssignment model) {
        return RoleAssignmentHistory.builder().actorId(model.getActorId())
            .actorIdTypeEnum(model.getActorIdTypeEnum())
            .attributes(convertValueJsonNode(model.getAttributes()))
            .beginTime(model.getBeginTime())
            .classification(model.getClassification())
            .endTime(model.getEndTime())
            .grantType(model.getGrantType())
            .roleName(model.getRoleName())
            .roleType(model.getRoleType())
            .status(model.getStatus())
            .readOnly(Boolean.TRUE)
            .build();
    }

    private void buildRoleAssignmentHistoryStatus(RoleAssignmentHistory roleAssignmentHistory) {
        RoleAssignmentHistoryStatus  roleAssignmentHistoryStatus =   RoleAssignmentHistoryStatus.builder().roleAssignmentHistory(roleAssignmentHistory)
            .log("professional drools rule")
            .status(Status.CREATED)
            .sequence(102)
            .build();
        roleAssignmentHistory.getRoleAssignmentHistoryStatus().add(roleAssignmentHistoryStatus);
    }

    public JsonNode convertValueJsonNode(Object from) {
        return objectMapper.convertValue(from, JsonNode.class);
    }
}
