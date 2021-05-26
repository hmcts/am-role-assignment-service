package uk.gov.hmcts.reform.roleassignment.domain.service.common;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyTestClass {
    public static void main(String[] args) {
        String tagValue = "(LD:feature_id_1=on)";
        List<String> tags = Arrays.stream(tagValue.split(","))
            .map(String::trim)
            .collect(Collectors.toList());

        tags.forEach(tag-> {
            String domain;
            String id = null;
            boolean expectedStatus;
            Boolean actualStatus;
            domain = tag.contains(":") ? tag.substring(tag.indexOf("(") + 1, tag.indexOf(":")) : "LD";
            if (!tag.contains(":") && !tag.contains("=")) {
                id = tag.substring(tag.indexOf("(") + 1, tag.indexOf(")"));
            } else if (tag.contains(":") && !tag.contains("=")) {
                id = tag.substring(tag.indexOf(":") + 1, tag.indexOf(")"));
            } else  if (tag.contains(":") && tag.contains("=")) {
                id = tag.substring(tag.indexOf(":") + 1, tag.indexOf("="));
            }

            if(tag.contains("=")) {
                String expectedStatusString = tag.substring(tag.indexOf("=") + 1, tag.indexOf(")"));
                expectedStatus = expectedStatusString.equalsIgnoreCase("on");
                expectedStatus = expectedStatusString.equalsIgnoreCase("on");
            }
            System.out.println("Id is : " + id);
        });
    }
}
