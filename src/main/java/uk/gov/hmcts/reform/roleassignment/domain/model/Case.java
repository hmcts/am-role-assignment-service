package uk.gov.hmcts.reform.roleassignment.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;

@Data
@Getter
@Setter
@Builder
public class Case {
    private String id;

    private String jurisdiction;

    @JsonProperty("case_type")
    private String caseTypeId;

    private Classification classification;
    private String region;
    private String baseLocation;

}
