package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class Case {
    private String id;

    @JsonIgnore
    private Long reference;

    @JsonProperty("version")
    private Integer version;

    private String jurisdiction;

    @JsonProperty("case_type")
    private String caseTypeId;

}
