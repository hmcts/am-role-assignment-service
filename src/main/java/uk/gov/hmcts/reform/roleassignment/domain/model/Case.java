package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
// all of the fields from CCD case details
public class Case {
    private String id;

    private String jurisdiction;

    @JsonProperty("case_type")
    private String caseTypeId;

}
