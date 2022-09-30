package uk.gov.hmcts.reform.roleassignment.feignclients.configuration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.roleassignment.domain.model.Case;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.feignclients.DataStoreApi;
import uk.gov.hmcts.reform.roleassignment.util.JacksonUtils;

import java.util.Map;

@Component
public class DataStoreApiFallback implements DataStoreApi {

    public static final String DATA_STORE_NOT_AVAILABLE = "The data store Service is not available";
    public static final String CIVIL = "CIVIL";
    public static final String LOCATION = "20262";

    @Override
    public String getServiceStatus() {
        return DATA_STORE_NOT_AVAILABLE;
    }

    @Override
    public Case getCaseDataV2(String caseId) {
        switch (caseId) {
            case "1234567890123456":
                return Case.builder().id(caseId)
                    .caseTypeId("CARE_SUPERVISION_EPO")
                    .jurisdiction("PUBLICLAW")
                    .securityClassification(Classification.PUBLIC)
                    .build();
            case "1114567890123455":
                return Case.builder().id(caseId)
                    .caseTypeId("PRLAPPS")
                    .jurisdiction("PRIVATELAW")
                    .securityClassification(Classification.PUBLIC)
                    .data(Map.of(Case.CASE_MANAGEMENT_LOCATION, JacksonUtils.convertValueJsonNode(
                        Map.of(Case.REGION,JacksonUtils.convertValueJsonNode(1),
                               Case.BASE_LOCATION, JacksonUtils.convertValueJsonNode(LOCATION)))))
                    .build();
            case "1114567890123456":
                return Case.builder().id(caseId)
                    .caseTypeId("DIVORCE")
                    .jurisdiction("DIVORCE")
                    .securityClassification(Classification.PUBLIC)
                    .build();
            case "1114567890123457":
                return Case.builder().id(caseId)
                    .caseTypeId("Benefit")
                    .jurisdiction("SSCS")
                    .securityClassification(Classification.PUBLIC)
                    .build();
            case "1114567890123458":
                return Case.builder().id(caseId)
                    .caseTypeId(CIVIL)
                    .jurisdiction(CIVIL)
                    .securityClassification(Classification.PUBLIC)
                    .data(Map.of(Case.CASE_MANAGEMENT_LOCATION, JacksonUtils.convertValueJsonNode(
                        Map.of(Case.REGION,JacksonUtils.convertValueJsonNode(1),
                               Case.BASE_LOCATION, JacksonUtils.convertValueJsonNode(LOCATION)))))
                    .build();
            case "1114567890123459":
                return Case.builder().id(caseId)
                    .caseTypeId("GENERALAPPLICATION")
                    .jurisdiction(CIVIL)
                    .securityClassification(Classification.PUBLIC)
                    .data(Map.of(Case.CASE_MANAGEMENT_LOCATION, JacksonUtils.convertValueJsonNode(
                        Map.of(Case.REGION,JacksonUtils.convertValueJsonNode(1),
                               Case.BASE_LOCATION, JacksonUtils.convertValueJsonNode(LOCATION)))))
                    .build();
            default:
                return Case.builder().id(caseId)
                    .caseTypeId("Asylum")
                    .jurisdiction("IA")
                    .securityClassification(Classification.PUBLIC)
                    .build();
        }
    }
}
