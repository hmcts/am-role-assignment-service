package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Getter
@Setter
@Builder
@ToString
public class Case {

    @JsonProperty("id")
    private String id;

    @JsonProperty("jurisdiction")
    private String jurisdiction;

    @JsonProperty("case_type")
    private String caseTypeId;

    @JsonProperty("created_on")
    private LocalDateTime createdOn;

    @JsonProperty("last_modified_on")
    private LocalDateTime lastModifiedOn;

    @JsonProperty("last_state_modified_on")
    private LocalDateTime lastStateModifiedOn;

    @JsonProperty("state")
    private String state;

    @JsonProperty("security_classification")
    //Convert from ccd.SecurityClassification to String
    private String securityClassification;

    @JsonProperty("data")
    private Map<String, JsonNode> data;

    @JsonProperty("data_classification")
    private Map<String, JsonNode> dataClassification;

}
