package uk.gov.hmcts.reform.roleassignment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.roleassignment.model.enums.Permission;

import java.util.List;

/**
 * Case Document.
 */
@Validated
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorisedService {

    @JsonProperty("id")
    private String id;

    @JsonProperty("caseTypeId")
    private String caseTypeId;

    @JsonProperty("jurisdictionId")
    private String jurisdictionId;

    @JsonProperty("permissions")
    private List<Permission> permissions;

}
