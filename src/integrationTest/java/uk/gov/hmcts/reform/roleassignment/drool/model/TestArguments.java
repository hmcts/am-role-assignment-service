package uk.gov.hmcts.reform.roleassignment.drool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.params.provider.Arguments;

public interface TestArguments {

    String getDescription();

    String getGroup();

    String getOutputSubFolder();

    String getService();

    @JsonIgnore
    default Arguments toArguments() {
        return Arguments.arguments(
            this.getDescription(),
            this
        );
    }

}
