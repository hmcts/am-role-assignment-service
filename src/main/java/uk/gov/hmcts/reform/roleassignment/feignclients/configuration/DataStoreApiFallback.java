package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;

@Component
public class DataStoreApiFallback implements DataStoreApi {

    public static final String DATA_STORE_NOT_AVAILABLE = "The data store Service is not available";

    @Override
    public String getServiceStatus() {
        return DATA_STORE_NOT_AVAILABLE;
    }

    @Override
    public Case getCaseDataV2(String caseId) {
        return switch (caseId) {
            case "1234567890123456" -> Case.builder().id(caseId)
                .caseTypeId("CARE_SUPERVISION_EPO")
                .jurisdiction("PUBLICLAW")
                .securityClassification(Classification.PUBLIC)
                .build();
            case "1114567890123455" -> Case.builder().id(caseId)
                .caseTypeId("PRLAPPS")
                .jurisdiction("PRIVATELAW")
                .securityClassification(Classification.PUBLIC)
                .build();
            case "1114567890123456" -> Case.builder().id(caseId)
                .caseTypeId("DIVORCE")
                .jurisdiction("DIVORCE")
                .securityClassification(Classification.PUBLIC)
                .build();
            case "1114567890123457" -> Case.builder().id(caseId)
                .caseTypeId("Benefit")
                .jurisdiction("SSCS")
                .securityClassification(Classification.PUBLIC)
                .build();
            case "1114567890123458" -> Case.builder().id(caseId)
                .caseTypeId("CIVIL")
                .jurisdiction("CIVIL")
                .securityClassification(Classification.PUBLIC)
                .build();
            case "1114567890123459" -> Case.builder().id(caseId)
                .caseTypeId("GENERALAPPLICATION")
                .jurisdiction("CIVIL")
                .securityClassification(Classification.PUBLIC)
                .build();
            default -> Case.builder().id(caseId)
                .caseTypeId("Asylum")
                .jurisdiction("IA")
                .securityClassification(Classification.PUBLIC)
                .build();
        };
    }
}
