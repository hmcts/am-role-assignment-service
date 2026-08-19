package uk.gov.hmcts.reform.roleassignment.drool.model;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.roleassignment.drool.BaseDroolIntegrationTest.writeObjectToDroolOutput;

@Getter
public class TestScenario {

    public static final String OUTPUT_FILE = "TestScenario";

    public TestScenario(String testName,
                        String testDescription,
                        String testOutputPathFormat,
                        TestArguments testArguments) {
        this.testName = testName;
        this.testDescription = testDescription;
        this.service = testArguments.getService();
        this.outputLocation = String.format(testOutputPathFormat, testArguments.getOutputSubFolder());
        this.testArguments = testArguments;
        this.steps = new ArrayList<>();
    }

    private final String testName;
    private final String testDescription;
    private final String service;
    private final String outputLocation;
    private final TestArguments testArguments;

    private final List<TestScenarioStep> steps;

    @Setter
    private Error error;

    public boolean hasError() {
        return error != null;
    }

    public void addFileToStep(String stepName, String fileName, Object data) {
        TestScenarioStep step = findOrAddStep(stepName);

        String outputFilePath = writeObjectToDroolOutput(
            data,
            this.outputLocation + "/" + step.getOutputFolder() + "/",
            fileName
        );

        if (StringUtils.isNotBlank(outputFilePath)) {
            step.getFiles().add(outputFilePath);
        }
    }

    @SneakyThrows
    public void addRasFilesToStep(String stepName, MvcResult result) {
        this.addFileToStep(stepName,
                           "rasCall_summary",
                           new TestRequestAndResponseSummary(result));
        this.addFileToStep(stepName,
                           "rasRequest_body",
                           result.getRequest().getContentAsString());
        this.addFileToStep(stepName,
                           "rasResponse_body_" + result.getResponse().getStatus(),
                           result.getResponse().getContentAsString());
    }

    private TestScenarioStep findOrAddStep(String stepName) {
        TestScenarioStep step = this.steps.stream()
            .filter(s -> s.getName().equals(stepName))
            .findFirst()
            .orElse(null);

        if (step == null) {
            step = TestScenarioStep.builder()
                .name(stepName)
                .order(this.steps.size() + 1)
                .build();

            this.steps.add(step);
        }

        return step;
    }

    public String writeToFile() {
        return writeObjectToDroolOutput(this, this.outputLocation, OUTPUT_FILE);
    }

}
