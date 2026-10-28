package uk.gov.hmcts.reform.roleassignment.drool.prmconfig;

import uk.gov.hmcts.reform.roleassignment.drool.model.PrmConfigTestArguments;

import java.util.ArrayList;
import java.util.List;

public class PublicLawPrmConfigIT {

    @SuppressWarnings({"LineLength"})
    public static List<PrmConfigTestArguments> getAllTestArguments() {
        List<PrmConfigTestArguments> arguments = new ArrayList<>();

        // generate tests for all PublicLaw professional GA-Group roles : CARE_SUPERVISION_EPO
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respa", "care_supervision_epo:all-case:solicitor-respa:101"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respb", "care_supervision_epo:all-case:solicitor-respb:102"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respc", "care_supervision_epo:all-case:solicitor-respc:103"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respd", "care_supervision_epo:all-case:solicitor-respd:104"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respe", "care_supervision_epo:all-case:solicitor-respe:105"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respf", "care_supervision_epo:all-case:solicitor-respf:106"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respg", "care_supervision_epo:all-case:solicitor-respg:107"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-resph", "care_supervision_epo:all-case:solicitor-resph:108"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respi", "care_supervision_epo:all-case:solicitor-respi:109"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-respj", "care_supervision_epo:all-case:solicitor-respj:110"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childa", "care_supervision_epo:all-case:solicitor-childa:201"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childb", "care_supervision_epo:all-case:solicitor-childb:202"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childc", "care_supervision_epo:all-case:solicitor-childc:203"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childd", "care_supervision_epo:all-case:solicitor-childd:204"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childe", "care_supervision_epo:all-case:solicitor-childe:205"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childf", "care_supervision_epo:all-case:solicitor-childf:206"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childg", "care_supervision_epo:all-case:solicitor-childg:207"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childh", "care_supervision_epo:all-case:solicitor-childh:208"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childi", "care_supervision_epo:all-case:solicitor-childi:209"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childj", "care_supervision_epo:all-case:solicitor-childj:210"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childk", "care_supervision_epo:all-case:solicitor-childk:211"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childl", "care_supervision_epo:all-case:solicitor-childl:212"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childm", "care_supervision_epo:all-case:solicitor-childm:213"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childn", "care_supervision_epo:all-case:solicitor-childn:214"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-childo", "care_supervision_epo:all-case:solicitor-childo:215"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-epsm", "care_supervision_epo:all-case:solicitor-epsm:111"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "la-primary", "care_supervision_epo:all-case:la-primary:222"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "la-secondary", "care_supervision_epo:all-case:la-secondary:333"));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "la-mla", "care_supervision_epo:all-case:la-mla:444"));

        // generate tests for all PublicLaw professional GA-Org roles : CARE_SUPERVISION_EPO
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "solicitor-create", null));
        arguments.add(createTestArguments("CARE_SUPERVISION_EPO", "la-create", null));

        return arguments;
    }

    @SuppressWarnings({"SameParameterValue"})
    private static PrmConfigTestArguments createTestArguments(String caseType,
                                                              String roleName,
                                                              String caseAccessGroupId) {
        return PrmConfigTestArguments.builder()
            // default test properties
            .serviceName("PublicLaw")
            .jurisdiction("PUBLICLAW")
            // test specific properties
            .caseType(caseType)
            .roleName(roleName)
            .caseAccessGroupId(caseAccessGroupId)
            .build();
    }

}

