package uk.gov.hmcts.reform.roleassignment.drool.helper;

import uk.gov.hmcts.reform.roleassignment.drool.BaseDroolIntegrationTest;
import uk.gov.hmcts.reform.roleassignment.drool.model.TestScenario;

import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ReportWriter {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);

    public static void writeTestReport(String reportName,
                                       String reportDescription,
                                       String outputLocation,
                                       List<TestScenario> testRun) {

        StringBuilder body = new StringBuilder();
        body.append(reportDescription);

        // group testRun by Service
        Map<String, List<TestScenario>> testRunByService = testRun.stream()
            .collect(Collectors.groupingBy(TestScenario::getService));

        testRunByService.keySet().stream()
            .sorted()
            .forEach(service -> {
                List<TestScenario> scenarios = testRunByService.get(service);

                String serviceReportLocation = writeServiceReport(service,
                                                                  reportName,
                                                                  outputLocation,
                                                                  scenarios);

                // Build the index for all service files.
                body.append(
                    HtmlBuilder.buildParagraph(
                        buildTickOrCross(scenarios)
                            + " "
                            + HtmlBuilder.buildRelativeHyperLink(outputLocation, serviceReportLocation, service),
                        getErrorColour(scenarios)
                    )
                );
            });

        appendFooter(body);

        BaseDroolIntegrationTest.createFile(outputLocation + "/index.html",
                                            HtmlBuilder.buildHtmlPage(reportName, body.toString()));
    }

    private static String writeServiceReport(String service,
                                             String reportName,
                                             String outputLocation,
                                             List<TestScenario> testScenarios) {

        String outputFolder = outputLocation + "/" + service;

        StringBuilder body = new StringBuilder();

        body.append(HtmlBuilder.buildHyperlink("../index.html", "Back to Index"));

        // group testScenarios by testArgument.group
        Map<String, List<TestScenario>> testScenarioByGroup = testScenarios.stream()
            .collect(Collectors.groupingBy(testScenario -> testScenario.getTestArguments().getGroup()));

        testScenarioByGroup.keySet().stream()
            .sorted()
            .forEach(testGroup -> {
                List<TestScenario> scenarios = testScenarioByGroup.get(testGroup);

                body.append(generateTestGroupSummary(testGroup, scenarios, outputFolder));
            });

        appendFooter(body);

        String outputFile = outputFolder + "/index.html";
        BaseDroolIntegrationTest.createFile(outputFolder + "/index.html",
                                            HtmlBuilder.buildHtmlPageWithCollapse(reportName + " - " + service,
                                                                                  body.toString()));
        return outputFile;
    }

    private static String generateTestGroupSummary(String groupName,
                                                   List<TestScenario> testScenarios,
                                                   String outputLocation) {

        StringBuilder body = new StringBuilder();

        body.append(HtmlBuilder.buildHeading2(groupName, getErrorColour(testScenarios)));

        // group testScenarios by testArgument.description
        Map<String, List<TestScenario>> testScenarioByArguments = testScenarios.stream()
            .collect(Collectors.groupingBy(testScenario -> testScenario.getTestArguments().getDescription()));

        StringBuilder groupSummary = new StringBuilder();

        testScenarioByArguments.keySet().stream()
            .sorted()
            .forEach(testArgumentDescription -> {
                List<TestScenario> scenarios = testScenarioByArguments.get(testArgumentDescription);

                groupSummary.append(HtmlBuilder.buildHeading3(testArgumentDescription, getErrorColour(scenarios)));

                scenarios.stream()
                    .sorted((s1, s2) -> s1.getTestDescription().compareTo(s2.getTestDescription()))
                    .forEach(testScenario ->
                                      groupSummary.append(generateTestScenarioSummary(testScenario, outputLocation))
                    );
            });

        body.append(HtmlBuilder.buildCollapseDiv(
            String.format("%s %d scenarios", buildTickOrCross(testScenarios), testScenarioByArguments.size()),
            groupSummary.toString(),
            testScenarios.stream().anyMatch(TestScenario::hasError)
        ));

        return body.toString();
    }

    private static String generateTestScenarioSummary(TestScenario testScenario,
                                                      String outputLocation) {

        StringBuilder body = new StringBuilder();

        if (testScenario.hasError()) {
            body.append(buildError(testScenario.getError()));
        }

        body.append("<ul>");
        testScenario.getSteps().forEach(step -> {
            body.append(String.format("<li>%s", step.getName()));

            body.append("<ul>");
            step.getFiles().forEach(file ->
                                        body.append(HtmlBuilder.buildLine(
                                            HtmlBuilder.buildRelativeHyperLink(outputLocation,
                                                                               file,
                                                                               Paths.get(file).getFileName().toString())
                                        ))
            );
            body.append("</ul></li>");
        });
        body.append(HtmlBuilder.buildLine(
            HtmlBuilder.buildRelativeHyperLink(
                outputLocation,
                testScenario.getOutputLocation() + "/" + TestScenario.OUTPUT_FILE + ".json",
                TestScenario.OUTPUT_FILE + ".json"
            )
        ));
        body.append("</ul>");

        return HtmlBuilder.buildCollapseDiv(
            HtmlBuilder.buildTickOrCross(!testScenario.hasError()) + " " + testScenario.getTestDescription(),
            body.toString(),
            testScenario.hasError()
        );
    }

    private static void appendFooter(StringBuilder body) {
        body.append(
            HtmlBuilder.buildDiv(
                null,
                "footer",
                String.format("Generated %s", dtf.format(ZonedDateTime.now(ZoneOffset.UTC)))
            )
        );
    }

    private static String buildError(Error error) {
        return HtmlBuilder.buildParagraph(
            HtmlBuilder.CROSS + " Error: " + HtmlBuilder.makeHtmlSafe(error.getMessage()),
            HtmlBuilder.getErrorColour(true)
        );
    }

    private static String buildTickOrCross(List<TestScenario> testScenarios) {
        return HtmlBuilder.buildTickOrCross(!hasError(testScenarios));
    }

    private static String getErrorColour(List<TestScenario> testScenarios) {
        return HtmlBuilder.getErrorColour(hasError(testScenarios));
    }

    private static boolean hasError(List<TestScenario> testScenarios) {
        return testScenarios.stream().anyMatch(TestScenario::hasError);
    }

}
