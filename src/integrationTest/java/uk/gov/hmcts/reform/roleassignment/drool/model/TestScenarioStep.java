package uk.gov.hmcts.reform.roleassignment.drool.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class TestScenarioStep {

    private String name;
    private int order;

    @Builder.Default
    private List<String> files = new ArrayList<>();

    @JsonIgnore
    public String getOutputFolder() {
        return String.format("%02d %s", order, name);
    }

}
