package uk.gov.hmcts.reform.roleassignment.domain.service.drools;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.roleassignment.domain.model.FeatureFlag;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.FeatureFlagEnum;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleCategory;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.RoleType;
import uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status;
import uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Classification.PUBLIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.GrantType.SPECIFIC;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.APPROVED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.DELETE_REQUESTED;
import static uk.gov.hmcts.reform.roleassignment.domain.model.enums.Status.REJECTED;
import static uk.gov.hmcts.reform.roleassignment.helper.TestDataBuilder.buildExistingRole;
import static uk.gov.hmcts.reform.roleassignment.util.JacksonUtils.convertValueJsonNode;

class CaseRolesDroolsTest extends DroolBase {


    @ParameterizedTest
    @CsvSource({
        // SSCS Benefit
        "SSCS,Benefit,hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,interloc-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,interloc-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,post-hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,case-allocator,JUDICIAL,RESTRICTED,case-allocator,N",
        "SSCS,Benefit,case-allocator,LEGAL_OPERATIONS,RESTRICTED,case-allocator,N",
        "SSCS,Benefit,registrar,LEGAL_OPERATIONS,RESTRICTED,registrar,Y",
        "SSCS,Benefit,allocated-tribunal-caseworker,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,regional-centre-admin,Y",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,clerk,Y",
        "SSCS,Benefit,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        // PRIVATELAW PRLAPPS
        "PRIVATELAW,PRLAPPS,hearing-judge,JUDICIAL,RESTRICTED,judge,",
        "PRIVATELAW,PRLAPPS,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PRIVATELAW,PRLAPPS,gatekeeping-judge,JUDICIAL,RESTRICTED,judge,",
        "PRIVATELAW,PRLAPPS,gatekeeping-judge,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PRIVATELAW,PRLAPPS,allocated-judge,JUDICIAL,RESTRICTED,judge,",
        "PRIVATELAW,PRLAPPS,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PRIVATELAW,PRLAPPS,allocated-magistrate,JUDICIAL,RESTRICTED,magistrate,",
        "PRIVATELAW,PRLAPPS,allocated-magistrate,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PRIVATELAW,PRLAPPS,allocated-legal-adviser,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "PRIVATELAW,PRLAPPS,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "PRIVATELAW,PRLAPPS,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "PRIVATELAW,PRLAPPS,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "PRIVATELAW,PRLAPPS,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "PRIVATELAW,PRLAPPS,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        // PUBLICLAW CARE_SUPERVISION_EPO
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-magistrate,JUDICIAL,RESTRICTED,magistrate,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-legal-adviser,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-legal-adviser,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        // EMPLOYMENT ET_EnglandWales
        "EMPLOYMENT,ET_EnglandWales,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,senior-tribunal-caseworker,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,Y",
        // EMPLOYMENT ET_EnglandWales_Multiple
        "EMPLOYMENT,ET_EnglandWales_Multiple,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,"
            + "tribunal-caseworker,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,"
            + "senior-tribunal-caseworker,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,Y",
        // EMPLOYMENT ET_Scotland
        "EMPLOYMENT,ET_Scotland,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "EMPLOYMENT,ET_Scotland,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,senior-tribunal-caseworker,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,Y",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,Y",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,Y",
        // EMPLOYMENT ET_Scotland_Multiple
        "EMPLOYMENT,ET_Scotland_Multiple,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,"
            + "senior-tribunal-caseworker,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,Y",
        // ST_CIC CriminalInjuriesCompensation
        "ST_CIC,CriminalInjuriesCompensation,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,interloc-judge,JUDICIAL,PUBLIC,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-legal-officer,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-legal-officer,LEGAL_OPERATIONS,RESTRICTED,"
            + "senior-tribunal-caseworker,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-administrator,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-administrator,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        // CIVIL CIVIL
        "CIVIL,CIVIL,lead-judge,JUDICIAL,RESTRICTED,leadership-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,circuit-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,district-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,deputy-district-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,recorder,Y",
        "CIVIL,CIVIL,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "CIVIL,CIVIL,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,senior-tribunal-caseworker,Y",
        "CIVIL,CIVIL,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "CIVIL,CIVIL,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "CIVIL,CIVIL,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "CIVIL,CIVIL,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "CIVIL,CIVIL,allocated-nbc-caseworker,ADMIN,RESTRICTED,national-business-centre,Y",
        "CIVIL,CIVIL,allocated-nbc-caseworker,ADMIN,RESTRICTED,nbc-team-leader,Y",
        //CIVIL GENERALAPPLICATION
        "CIVIL,GENERALAPPLICATION,lead-judge,JUDICIAL,RESTRICTED,leadership-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,circuit-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,district-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,deputy-district-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,recorder,Y",
        "CIVIL,GENERALAPPLICATION,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "CIVIL,GENERALAPPLICATION,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,senior-tribunal-caseworker,Y",
        "CIVIL,GENERALAPPLICATION,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "CIVIL,GENERALAPPLICATION,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "CIVIL,GENERALAPPLICATION,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "CIVIL,GENERALAPPLICATION,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "CIVIL,GENERALAPPLICATION,allocated-nbc-caseworker,ADMIN,RESTRICTED,national-business-centre,Y",
        "CIVIL,GENERALAPPLICATION,allocated-nbc-caseworker,ADMIN,RESTRICTED,nbc-team-leader,Y",
        // PROBATE
        "PROBATE,GrantOfRepresentation,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "PROBATE,GrantOfRepresentation,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "PROBATE,GrantOfRepresentation,allocated-tribunal-caseworker,LEGAL_OPERATIONS,RESTRICTED,"
            + "senior-tribunal-caseworker,Y",
        // POSSESSIONS
        "PCS,any-case-type,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "PCS,any-case-type,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "PCS,any-case-type,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "PCS,any-case-type,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "PCS,any-case-type,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "PCS,any-case-type,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "PCS,any-case-type,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "PCS,any-case-type,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "PCS,any-case-type,allocated-admin-caseworker,ADMIN,RESTRICTED,bailiff-admin,Y",
        "PCS,any-case-type,allocated-wlu-caseworker,ADMIN,RESTRICTED,wlu-admin,Y",
        "PCS,any-case-type,allocated-wlu-caseworker,ADMIN,RESTRICTED,wlu-team-leader,Y",
        "PCS,any-case-type,case-allocator,JUDICIAL,RESTRICTED,case-allocator,N",
        "PCS,any-case-type,case-allocator,ADMIN,RESTRICTED,case-allocator,N",
        "PCS,any-case-type,case-allocator,CTSC,RESTRICTED,case-allocator,N",
        // FR: Consented
        "DIVORCE,FinancialRemedyMVP2,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "DIVORCE,FinancialRemedyMVP2,lead-judge,JUDICIAL,RESTRICTED,judge,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,national-business-centre,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,nbc-team-leader,Y",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,JUDICIAL,RESTRICTED,case-allocator,N",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,ADMIN,RESTRICTED,case-allocator,N",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,CTSC,RESTRICTED,case-allocator,N",
    })
    void shouldGrantAccessFor_CaseRole(String jurisdiction, String caseType, String roleName,
                                       String roleCategory, String classification,
                                       String existingRoleName, String expectedSubstantive) {

        verifyGrantOrRejectAccessFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            roleCategory,
            classification,
            existingRoleName,
            caseType,
            null,
            null,
            expectedSubstantive,
            APPROVED
        );

        // repeat with valid CA region filter
        verifyGrantOrRejectAccessFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            roleCategory,
            classification,
            existingRoleName,
            caseType,
            CASE_REGION,
            null,
            expectedSubstantive,
            APPROVED
        );

        // repeat with valid CA case-type filter
        verifyGrantOrRejectAccessFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            roleCategory,
            classification,
            existingRoleName,
            caseType,
            null,
            caseType,
            expectedSubstantive,
            APPROVED
        );
    }


    @ParameterizedTest
    @CsvSource({
        // SSCS Benefit
        "SSCS,Benefit,hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-1,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-2,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,tribunal-member-3,JUDICIAL,RESTRICTED,fee-paid-tribunal-member,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,appraiser-1,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,medical,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-medical,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-disability,Y",
        "SSCS,Benefit,appraiser-2,JUDICIAL,RESTRICTED,fee-paid-financial,Y",
        "SSCS,Benefit,interloc-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,interloc-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "SSCS,Benefit,post-hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "SSCS,Benefit,case-allocator,JUDICIAL,RESTRICTED,case-allocator,N",
        "SSCS,Benefit,case-allocator,LEGAL_OPERATIONS,RESTRICTED,case-allocator,N",
        "SSCS,Benefit,registrar,LEGAL_OPERATIONS,RESTRICTED,registrar,Y",
        "SSCS,Benefit,allocated-tribunal-caseworker,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,regional-centre-admin,Y",
        "SSCS,Benefit,allocated-admin-caseworker,ADMIN,RESTRICTED,clerk,Y",
        "SSCS,Benefit,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        // PRIVATELAW PRLAPPS
        "PRIVATELAW,PRLAPPS,hearing-judge,JUDICIAL,RESTRICTED,judge,",
        "PRIVATELAW,PRLAPPS,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PRIVATELAW,PRLAPPS,gatekeeping-judge,JUDICIAL,RESTRICTED,judge,",
        "PRIVATELAW,PRLAPPS,gatekeeping-judge,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PRIVATELAW,PRLAPPS,allocated-judge,JUDICIAL,RESTRICTED,judge,",
        "PRIVATELAW,PRLAPPS,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PRIVATELAW,PRLAPPS,allocated-magistrate,JUDICIAL,RESTRICTED,magistrate,",
        "PRIVATELAW,PRLAPPS,allocated-magistrate,JUDICIAL,RESTRICTED,fee-paid-judge,",
        "PRIVATELAW,PRLAPPS,allocated-legal-adviser,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "PRIVATELAW,PRLAPPS,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "PRIVATELAW,PRLAPPS,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "PRIVATELAW,PRLAPPS,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "PRIVATELAW,PRLAPPS,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "PRIVATELAW,PRLAPPS,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        // PUBLICLAW CARE_SUPERVISION_EPO
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-magistrate,JUDICIAL,RESTRICTED,magistrate,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-legal-adviser,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-legal-adviser,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "PUBLICLAW,CARE_SUPERVISION_EPO,hearing-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        // EMPLOYMENT ET_EnglandWales
        "EMPLOYMENT,ET_EnglandWales,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,senior-tribunal-caseworker,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,Y",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,Y",
        // EMPLOYMENT ET_EnglandWales_Multiple
        "EMPLOYMENT,ET_EnglandWales_Multiple,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,"
            + "tribunal-caseworker,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,"
            + "senior-tribunal-caseworker,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,Y",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,Y",
        // EMPLOYMENT ET_Scotland
        "EMPLOYMENT,ET_Scotland,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "EMPLOYMENT,ET_Scotland,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,senior-tribunal-caseworker,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,Y",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,Y",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,Y",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,Y",
        // EMPLOYMENT ET_Scotland_Multiple
        "EMPLOYMENT,ET_Scotland_Multiple,lead-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,lead-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge,JUDICIAL,PUBLIC,leadership-judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-1,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-2,JUDICIAL,PUBLIC,tribunal-member,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,tribunal-caseworker,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC,"
            + "senior-tribunal-caseworker,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-admin,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,hearing-centre-team-leader,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-admin,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,regional-centre-team-leader,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC,clerk,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc,Y",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC,ctsc-team-leader,Y",
        // ST_CIC CriminalInjuriesCompensation
        "ST_CIC,CriminalInjuriesCompensation,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,interloc-judge,JUDICIAL,PUBLIC,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,fee-paid-medical,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,fee-paid-tribunal-member,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,fee-paid-disability,Y",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC,fee-paid-financial,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-legal-officer,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-legal-officer,LEGAL_OPERATIONS,RESTRICTED,"
            + "senior-tribunal-caseworker,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-administrator,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-administrator,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "ST_CIC,CriminalInjuriesCompensation,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        // CIVIL CIVIL
        "CIVIL,CIVIL,lead-judge,JUDICIAL,RESTRICTED,leadership-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,circuit-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,district-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,deputy-district-judge,Y",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED,recorder,Y",
        "CIVIL,CIVIL,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "CIVIL,CIVIL,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,senior-tribunal-caseworker,Y",
        "CIVIL,CIVIL,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "CIVIL,CIVIL,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "CIVIL,CIVIL,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "CIVIL,CIVIL,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "CIVIL,CIVIL,allocated-nbc-caseworker,ADMIN,RESTRICTED,national-business-centre,Y",
        "CIVIL,CIVIL,allocated-nbc-caseworker,ADMIN,RESTRICTED,nbc-team-leader,Y",
        //CIVIL GENERALAPPLICATION
        "CIVIL,GENERALAPPLICATION,lead-judge,JUDICIAL,RESTRICTED,leadership-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,circuit-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,district-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,deputy-district-judge,Y",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED,recorder,Y",
        "CIVIL,GENERALAPPLICATION,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,tribunal-caseworker,Y",
        "CIVIL,GENERALAPPLICATION,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED,senior-tribunal-caseworker,Y",
        "CIVIL,GENERALAPPLICATION,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "CIVIL,GENERALAPPLICATION,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "CIVIL,GENERALAPPLICATION,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "CIVIL,GENERALAPPLICATION,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "CIVIL,GENERALAPPLICATION,allocated-nbc-caseworker,ADMIN,RESTRICTED,national-business-centre,Y",
        "CIVIL,GENERALAPPLICATION,allocated-nbc-caseworker,ADMIN,RESTRICTED,nbc-team-leader,Y",
        // PROBATE
        "PROBATE,GrantOfRepresentation,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "PROBATE,GrantOfRepresentation,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "PROBATE,GrantOfRepresentation,allocated-tribunal-caseworker,LEGAL_OPERATIONS,RESTRICTED,"
            + "senior-tribunal-caseworker,Y",
        // POSSESSIONS
        "PCS,any-case-type,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "PCS,any-case-type,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "PCS,any-case-type,hearing-judge,JUDICIAL,PUBLIC,judge,Y",
        "PCS,any-case-type,hearing-judge,JUDICIAL,PUBLIC,fee-paid-judge,Y",
        "PCS,any-case-type,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "PCS,any-case-type,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "PCS,any-case-type,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "PCS,any-case-type,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "PCS,any-case-type,allocated-admin-caseworker,ADMIN,RESTRICTED,bailiff-admin,Y",
        "PCS,any-case-type,allocated-wlu-caseworker,ADMIN,RESTRICTED,wlu-admin,Y",
        "PCS,any-case-type,allocated-wlu-caseworker,ADMIN,RESTRICTED,wlu-team-leader,Y",
        "PCS,any-case-type,case-allocator,JUDICIAL,RESTRICTED,case-allocator,N",
        "PCS,any-case-type,case-allocator,ADMIN,RESTRICTED,case-allocator,N",
        "PCS,any-case-type,case-allocator,CTSC,RESTRICTED,case-allocator,N",
        // FR: Consented
        "DIVORCE,FinancialRemedyMVP2,allocated-judge,JUDICIAL,RESTRICTED,judge,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge,JUDICIAL,RESTRICTED,judge,Y",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge,Y",
        "DIVORCE,FinancialRemedyMVP2,lead-judge,JUDICIAL,RESTRICTED,judge,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,national-business-centre,Y",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,nbc-team-leader,Y",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,JUDICIAL,RESTRICTED,case-allocator,N",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,ADMIN,RESTRICTED,case-allocator,N",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,CTSC,RESTRICTED,case-allocator,N",
    })
    void shouldRejectAccessFor_CaseRole_withBadCaseAllocatorRegionOrCaseType(String jurisdiction,
                                                                             String caseType,
                                                                             String roleName,
                                                                             String roleCategory,
                                                                             String classification,
                                                                             String existingRoleName,
                                                                             String expectedSubstantive) {
        // with invalid CA region filter
        verifyGrantOrRejectAccessFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            roleCategory,
            classification,
            existingRoleName,
            caseType,
            "bad-region",
            null,
            expectedSubstantive,
            Status.DELETE_REJECTED
        );

        // with invalid CA case-type filter
        verifyGrantOrRejectAccessFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            roleCategory,
            classification,
            existingRoleName,
            caseType,
            null,
            "bad-case-type",
            expectedSubstantive,
            Status.DELETE_REJECTED
        );
    }


    @ParameterizedTest
    @CsvSource({
        // FR: Consented
        "DIVORCE,FinancialRemedyMVP2,allocated-judge,JUDICIAL,RESTRICTED,judge",
        "DIVORCE,FinancialRemedyMVP2,allocated-judge,JUDICIAL,RESTRICTED,fee-paid-judge",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge,JUDICIAL,RESTRICTED,judge",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge,JUDICIAL,RESTRICTED,fee-paid-judge",
        "DIVORCE,FinancialRemedyMVP2,lead-judge,JUDICIAL,RESTRICTED,judge",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker,CTSC,RESTRICTED,ctsc-team-leader",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-admin",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,hearing-centre-team-leader",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,national-business-centre",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED,nbc-team-leader",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,JUDICIAL,RESTRICTED,case-allocator",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,ADMIN,RESTRICTED,case-allocator",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,CTSC,RESTRICTED,case-allocator",
    })
    // NB: test only needed when validation of case-type on existing role-assignment is enforced
    void shouldRejectAccessFor_CaseRole_BadExistingRoleCaseType(String jurisdiction, String caseType, String roleName,
                                                                String roleCategory, String classification,
                                                                String existingRoleName) {
        verifyGrantOrRejectAccessFor_CaseRole(jurisdiction,
                                              caseType,
                                              roleName,
                                              roleCategory,
                                              classification,
                                              existingRoleName,
                                              "bad-case-type",
                                              null,
                                              null,
                                              null,
                                              REJECTED);
    }


    @ParameterizedTest
    @CsvSource({
        // EMPLOYMENT ET_EnglandWales
        "EMPLOYMENT,ET_EnglandWales,lead-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-1,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker,ADMIN,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker,CTSC,PUBLIC",
        // EMPLOYMENT ET_EnglandWales_Multiple
        "EMPLOYMENT,ET_EnglandWales_Multiple,lead-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-1,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-2,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC",
        // EMPLOYMENT ET_Scotland
        "EMPLOYMENT,ET_Scotland,lead-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland,hearing-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland,tribunal-member-1,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland,tribunal-member-2,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker,ADMIN,PUBLIC",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker,CTSC,PUBLIC",
        // EMPLOYMENT ET_Scotland_Multiple
        "EMPLOYMENT,ET_Scotland_Multiple,lead-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-1,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-2,JUDICIAL,PUBLIC",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-tribunal-caseworker,LEGAL_OPERATIONS,PUBLIC",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker,ADMIN,PUBLIC",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-ctsc-caseworker,CTSC,PUBLIC",
        // ST_CIC CriminalInjuriesCompensation
        "ST_CIC,CriminalInjuriesCompensation,hearing-judge,JUDICIAL,PUBLIC",
        "ST_CIC,CriminalInjuriesCompensation,interloc-judge,JUDICIAL,PUBLIC",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1,JUDICIAL,PUBLIC",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2,JUDICIAL,PUBLIC",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-3,JUDICIAL,PUBLIC",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1,JUDICIAL,PUBLIC",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2,JUDICIAL,PUBLIC",
        "ST_CIC,CriminalInjuriesCompensation,allocated-legal-officer,LEGAL_OPERATIONS,RESTRICTED",
        "ST_CIC,CriminalInjuriesCompensation,allocated-administrator,ADMIN,RESTRICTED",
        "ST_CIC,CriminalInjuriesCompensation,allocated-judge,JUDICIAL,RESTRICTED",
        // PRIVATELAW PRLAPPS
        "PRIVATELAW,PRLAPPS,hearing-judge,JUDICIAL,RESTRICTED",
        "PRIVATELAW,PRLAPPS,allocated-magistrate,JUDICIAL,RESTRICTED",
        "PRIVATELAW,PRLAPPS,allocated-judge,JUDICIAL,RESTRICTED",
        "PRIVATELAW,PRLAPPS,gatekeeping-judge,JUDICIAL,RESTRICTED",
        "PRIVATELAW,PRLAPPS,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED",
        "PRIVATELAW,PRLAPPS,allocated-ctsc-caseworker,CTSC,RESTRICTED",
        "PRIVATELAW,PRLAPPS,allocated-admin-caseworker,ADMIN,RESTRICTED",
        // CIVIL CIVIL
        "CIVIL,CIVIL,lead-judge,JUDICIAL,RESTRICTED",
        "CIVIL,CIVIL,allocated-judge,JUDICIAL,RESTRICTED",
        "CIVIL,CIVIL,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED",
        "CIVIL,CIVIL,allocated-admin-caseworker,ADMIN,RESTRICTED",
        "CIVIL,CIVIL,allocated-ctsc-caseworker,CTSC,RESTRICTED",
        "CIVIL,CIVIL,allocated-nbc-caseworker,ADMIN,RESTRICTED",
        //CIVIL GENERALAPPLICATION
        "CIVIL,GENERALAPPLICATION,lead-judge,JUDICIAL,RESTRICTED",
        "CIVIL,GENERALAPPLICATION,allocated-judge,JUDICIAL,RESTRICTED",
        "CIVIL,GENERALAPPLICATION,allocated-legal-adviser,LEGAL_OPERATIONS,RESTRICTED",
        "CIVIL,GENERALAPPLICATION,allocated-admin-caseworker,ADMIN,RESTRICTED",
        "CIVIL,GENERALAPPLICATION,allocated-ctsc-caseworker,CTSC,RESTRICTED",
        "CIVIL,GENERALAPPLICATION,allocated-nbc-caseworker,ADMIN,RESTRICTED",
        // POSSESSIONS
        "PCS,any-case-type,allocated-judge,JUDICIAL,RESTRICTED",
        "PCS,any-case-type,hearing-judge,JUDICIAL,RESTRICTED",
        "PCS,any-case-type,allocated-ctsc-caseworker,CTSC,RESTRICTED",
        "PCS,any-case-type,allocated-admin-caseworker,ADMIN,RESTRICTED",
        "PCS,any-case-type,allocated-wlu-caseworker,ADMIN,RESTRICTED",
        "PCS,any-case-type,allocated-bailiff,ADMIN,RESTRICTED",
        "PCS,any-case-type,case-allocator,JUDICIAL,RESTRICTED",
        "PCS,any-case-type,case-allocator,ADMIN,RESTRICTED",
        "PCS,any-case-type,case-allocator,CTSC,RESTRICTED",
        // FR: Consented
        "DIVORCE,FinancialRemedyMVP2,allocated-judge,JUDICIAL,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge,JUDICIAL,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,lead-judge,JUDICIAL,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker,CTSC,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,JUDICIAL,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,ADMIN,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,CTSC,RESTRICTED",
    })
    void shouldRejectAccessFor_CaseRole_BadExistingRoleName(String jurisdiction, String caseType, String roleName,
                                                            String roleCategory, String classification) {
        verifyGrantOrRejectAccessFor_CaseRole(jurisdiction,
                                              caseType,
                                              roleName,
                                              roleCategory,
                                              classification,
                                              "bad-role-name",
                                              caseType,
                                              null,
                                              null,
                                              null,
                                              REJECTED);
    }

    private void verifyGrantOrRejectAccessFor_CaseRole(String jurisdiction, String caseType, String roleName,
                                                       String roleCategory, String classification,
                                                       String existingRoleName, String existingCaseType,
                                                       String caRegion, String caCaseType,
                                                       String expectedSubstantive,
                                                       Status expectedRoleAssignmentStatus) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode(getCaseFromMap(jurisdiction, caseType).getId()));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode(caseType));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
            "sscs-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            RoleType.CASE,
            roleAssignmentAttributes,
            Classification.valueOf(classification),
            SPECIFIC,
            Status.CREATE_REQUESTED,
            TestDataBuilder.CLIENT_ID_XUI,
            false,
            "Access required for reasons",
            TestDataBuilder.ACTORID,
            "reference"
        )
            .build();

        setFeatureFlags();

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        existingAttributes.put("caseType", convertValueJsonNode(existingCaseType));

        HashMap<String, JsonNode> existingCaAttributes = new HashMap<>();
        existingCaAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        if (StringUtils.isNotBlank(caCaseType)) {
            existingCaAttributes.put("caseType", convertValueJsonNode(caCaseType));
        }
        if (StringUtils.isNotBlank(caRegion)) {
            existingCaAttributes.put("region", convertValueJsonNode(caRegion));
        }

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingCaAttributes,
                                          PUBLIC,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      ),
                                  TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.ACTORID,
                                          existingRoleName,
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          PUBLIC,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      )
                                )
                          );

        if (expectedRoleAssignmentStatus == APPROVED) {
            assignmentRequest.getRequestedRoles().forEach(roleAssignment -> {
                assertEquals(TestDataBuilder.ACTORID, roleAssignment.getActorId());
                assertEquals(jurisdiction, roleAssignment.getAttributes().get("jurisdiction").asText());
                assertEquals(caseType, roleAssignment.getAttributes().get("caseType").asText());
                assertEquals(roleName, roleAssignment.getRoleName());
                assertEquals(RoleCategory.valueOf(roleCategory), roleAssignment.getRoleCategory());
                assertEquals(Classification.valueOf(classification), roleAssignment.getClassification());
                if (expectedSubstantive != null) {
                    assertEquals(expectedSubstantive, roleAssignment.getAttributes().get("substantive").asText());
                }
                assertEquals(Status.APPROVED, roleAssignment.getStatus());
            });
        } else {
            assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.REJECTED, ra.getStatus()));
        }
    }

    @ParameterizedTest
    @CsvSource({
        // SSCS Benefit
        "SSCS,Benefit,hearing-judge",
        "SSCS,Benefit,tribunal-member-1",
        "SSCS,Benefit,tribunal-member-2",
        "SSCS,Benefit,tribunal-member-3",
        "SSCS,Benefit,appraiser-1",
        "SSCS,Benefit,appraiser-2",
        "SSCS,Benefit,interloc-judge",
        "SSCS,Benefit,post-hearing-judge",
        "SSCS,Benefit,case-allocator",
        "SSCS,Benefit,registrar",
        "SSCS,Benefit,allocated-tribunal-caseworker",
        "SSCS,Benefit,allocated-admin-caseworker",
        "SSCS,Benefit,allocated-ctsc-caseworker",
        // PRIVATELAW PRLAPPS
        "PRIVATELAW,PRLAPPS,hearing-judge",
        "PRIVATELAW,PRLAPPS,allocated-magistrate",
        "PRIVATELAW,PRLAPPS,allocated-judge",
        "PRIVATELAW,PRLAPPS,gatekeeping-judge",
        "PRIVATELAW,PRLAPPS,allocated-legal-adviser",
        "PRIVATELAW,PRLAPPS,allocated-ctsc-caseworker",
        "PRIVATELAW,PRLAPPS,allocated-admin-caseworker",
        // EMPLOYMENT ET_EnglandWales
        "EMPLOYMENT,ET_EnglandWales,lead-judge",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-1",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker",
        // EMPLOYMENT ET_EnglandWales_Multiple
        "EMPLOYMENT,ET_EnglandWales_Multiple,lead-judge",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-1",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-2",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-ctsc-caseworker",
        // EMPLOYMENT ET_Scotland
        "EMPLOYMENT,ET_Scotland,lead-judge",
        "EMPLOYMENT,ET_Scotland,hearing-judge",
        "EMPLOYMENT,ET_Scotland,tribunal-member-1",
        "EMPLOYMENT,ET_Scotland,tribunal-member-2",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker",
        // EMPLOYMENT ET_Scotland_Multiple
        "EMPLOYMENT,ET_Scotland_Multiple,lead-judge",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-1",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-2",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-ctsc-caseworker",
        // ST_CIC CriminalInjuriesCompensation
        "ST_CIC,CriminalInjuriesCompensation,hearing-judge",
        "ST_CIC,CriminalInjuriesCompensation,interloc-judge",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2",
        //CIVIL Civil
        "CIVIL,CIVIL,lead-judge",
        "CIVIL,CIVIL,allocated-judge",
        "CIVIL,CIVIL,allocated-legal-adviser",
        "CIVIL,CIVIL,allocated-admin-caseworker",
        "CIVIL,CIVIL,allocated-ctsc-caseworker",
        "CIVIL,CIVIL,allocated-nbc-caseworker",
        //CIVIL GENERALAPPLICATION
        "CIVIL,GENERALAPPLICATION,lead-judge",
        "CIVIL,GENERALAPPLICATION,allocated-judge",
        "CIVIL,GENERALAPPLICATION,allocated-legal-adviser",
        "CIVIL,GENERALAPPLICATION,allocated-admin-caseworker",
        "CIVIL,GENERALAPPLICATION,allocated-ctsc-caseworker",
        "CIVIL,GENERALAPPLICATION,allocated-nbc-caseworker",
        // POSSESSIONS
        "PCS,any-case-type,allocated-judge",
        "PCS,any-case-type,hearing-judge",
        "PCS,any-case-type,allocated-ctsc-caseworker",
        "PCS,any-case-type,allocated-admin-caseworker",
        "PCS,any-case-type,allocated-wlu-caseworker",
        "PCS,any-case-type,allocated-bailiff",
        "PCS,any-case-type,case-allocator",
        // FR: Consented
        "DIVORCE,FinancialRemedyMVP2,allocated-judge,JUDICIAL,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge,JUDICIAL,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,lead-judge,JUDICIAL,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker,CTSC,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker,ADMIN,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,JUDICIAL,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,ADMIN,RESTRICTED",
        "DIVORCE,FinancialRemedyMVP2,case-allocator,CTSC,RESTRICTED",
    })
    void shouldGrantDeleteFor_CaseRole(String jurisdiction, String caseType, String roleName) {

        verifyGrantOrRejectDeleteFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            null,
            null,
            Status.DELETE_APPROVED
        );

        // repeat with valid CA region filter
        verifyGrantOrRejectDeleteFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            CASE_REGION,
            null,
            Status.DELETE_APPROVED
        );

        // with valid CA case-type filter
        verifyGrantOrRejectDeleteFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            null,
            caseType,
            Status.DELETE_APPROVED
        );
    }

    @ParameterizedTest
    @CsvSource({
        // SSCS Benefit
        "SSCS,Benefit,hearing-judge",
        "SSCS,Benefit,tribunal-member-1",
        "SSCS,Benefit,tribunal-member-2",
        "SSCS,Benefit,tribunal-member-3",
        "SSCS,Benefit,appraiser-1",
        "SSCS,Benefit,appraiser-2",
        "SSCS,Benefit,interloc-judge",
        "SSCS,Benefit,post-hearing-judge",
        "SSCS,Benefit,case-allocator",
        "SSCS,Benefit,registrar",
        "SSCS,Benefit,allocated-tribunal-caseworker",
        "SSCS,Benefit,allocated-admin-caseworker",
        "SSCS,Benefit,allocated-ctsc-caseworker",
        // PRIVATELAW PRLAPPS
        "PRIVATELAW,PRLAPPS,hearing-judge",
        "PRIVATELAW,PRLAPPS,allocated-magistrate",
        "PRIVATELAW,PRLAPPS,allocated-judge",
        "PRIVATELAW,PRLAPPS,gatekeeping-judge",
        "PRIVATELAW,PRLAPPS,allocated-legal-adviser",
        "PRIVATELAW,PRLAPPS,allocated-ctsc-caseworker",
        "PRIVATELAW,PRLAPPS,allocated-admin-caseworker",
        // EMPLOYMENT ET_EnglandWales
        "EMPLOYMENT,ET_EnglandWales,lead-judge",
        "EMPLOYMENT,ET_EnglandWales,hearing-judge",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-1",
        "EMPLOYMENT,ET_EnglandWales,tribunal-member-2",
        "EMPLOYMENT,ET_EnglandWales,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_EnglandWales,allocated-admin-caseworker",
        "EMPLOYMENT,ET_EnglandWales,allocated-ctsc-caseworker",
        // EMPLOYMENT ET_EnglandWales_Multiple
        "EMPLOYMENT,ET_EnglandWales_Multiple,lead-judge",
        "EMPLOYMENT,ET_EnglandWales_Multiple,hearing-judge",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-1",
        "EMPLOYMENT,ET_EnglandWales_Multiple,tribunal-member-2",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-admin-caseworker",
        "EMPLOYMENT,ET_EnglandWales_Multiple,allocated-ctsc-caseworker",
        // EMPLOYMENT ET_Scotland
        "EMPLOYMENT,ET_Scotland,lead-judge",
        "EMPLOYMENT,ET_Scotland,hearing-judge",
        "EMPLOYMENT,ET_Scotland,tribunal-member-1",
        "EMPLOYMENT,ET_Scotland,tribunal-member-2",
        "EMPLOYMENT,ET_Scotland,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_Scotland,allocated-admin-caseworker",
        "EMPLOYMENT,ET_Scotland,allocated-ctsc-caseworker",
        // EMPLOYMENT ET_Scotland_Multiple
        "EMPLOYMENT,ET_Scotland_Multiple,lead-judge",
        "EMPLOYMENT,ET_Scotland_Multiple,hearing-judge",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-1",
        "EMPLOYMENT,ET_Scotland_Multiple,tribunal-member-2",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-tribunal-caseworker",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-admin-caseworker",
        "EMPLOYMENT,ET_Scotland_Multiple,allocated-ctsc-caseworker",
        // ST_CIC CriminalInjuriesCompensation
        "ST_CIC,CriminalInjuriesCompensation,hearing-judge",
        "ST_CIC,CriminalInjuriesCompensation,interloc-judge",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-1",
        "ST_CIC,CriminalInjuriesCompensation,tribunal-member-2",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-1",
        "ST_CIC,CriminalInjuriesCompensation,appraiser-2",
        //CIVIL Civil
        "CIVIL,CIVIL,lead-judge",
        "CIVIL,CIVIL,allocated-judge",
        "CIVIL,CIVIL,allocated-legal-adviser",
        "CIVIL,CIVIL,allocated-admin-caseworker",
        "CIVIL,CIVIL,allocated-ctsc-caseworker",
        "CIVIL,CIVIL,allocated-nbc-caseworker",
        //CIVIL GENERALAPPLICATION
        "CIVIL,GENERALAPPLICATION,lead-judge",
        "CIVIL,GENERALAPPLICATION,allocated-judge",
        "CIVIL,GENERALAPPLICATION,allocated-legal-adviser",
        "CIVIL,GENERALAPPLICATION,allocated-admin-caseworker",
        "CIVIL,GENERALAPPLICATION,allocated-ctsc-caseworker",
        "CIVIL,GENERALAPPLICATION,allocated-nbc-caseworker",
        // POSSESSIONS
        "PCS,any-case-type,allocated-judge",
        "PCS,any-case-type,hearing-judge",
        "PCS,any-case-type,allocated-ctsc-caseworker",
        "PCS,any-case-type,allocated-admin-caseworker",
        "PCS,any-case-type,allocated-wlu-caseworker",
        "PCS,any-case-type,allocated-bailiff",
        "PCS,any-case-type,case-allocator",
        // FR: Consented
        "DIVORCE,FinancialRemedyMVP2,allocated-judge",
        "DIVORCE,FinancialRemedyMVP2,hearing-judge",
        "DIVORCE,FinancialRemedyMVP2,lead-judge",
        "DIVORCE,FinancialRemedyMVP2,allocated-ctsc-caseworker",
        "DIVORCE,FinancialRemedyMVP2,allocated-admin-caseworker",
        "DIVORCE,FinancialRemedyMVP2,case-allocator",
        "DIVORCE,FinancialRemedyMVP2,case-allocator",
        "DIVORCE,FinancialRemedyMVP2,case-allocator",
    })
    void shouldRejectDeleteFor_CaseRole_withBadCaseAllocatorRegionOrCaseType(String jurisdiction,
                                                                             String caseType,
                                                                             String roleName) {
        // with invalid CA region filter
        verifyGrantOrRejectDeleteFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            "bad-region",
            null,
            Status.DELETE_REJECTED
        );

        // with invalid CA case-type filter
        verifyGrantOrRejectDeleteFor_CaseRole(
            jurisdiction,
            caseType,
            roleName,
            null,
            "bad-case-type",
            Status.DELETE_REJECTED
        );
    }

    private void verifyGrantOrRejectDeleteFor_CaseRole(String jurisdiction,
                                                       String roleCaseType,
                                                       String roleName,
                                                       String caRegion,
                                                       String caCaseType,
                                                       Status expectedRoleAssignmentStatus) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode(roleCaseType));
        roleAssignmentAttributes
            .put("caseId", convertValueJsonNode(getCaseFromMap(jurisdiction, roleCaseType).getId()));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
            "delete-access",
            roleName,
            RoleCategory.valueOf(RoleCategory.JUDICIAL.name()),
            RoleType.CASE,
            roleAssignmentAttributes,
            PUBLIC,
            SPECIFIC,
            DELETE_REQUESTED,
            TestDataBuilder.CLIENT_ID_XUI,
            false,
            "Delete required for reasons",
            TestDataBuilder.ACTORID,
            "reference"
        )
            .build();

        setFeatureFlags();

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        if (StringUtils.isNotBlank(caCaseType)) {
            existingAttributes.put("caseType", convertValueJsonNode(caCaseType));
        }
        if (StringUtils.isNotBlank(caRegion)) {
            existingAttributes.put("region", convertValueJsonNode(caRegion));
        }
        existingAttributes.put("allocatedRole", convertValueJsonNode(roleName));

        executeDroolRules(List.of(buildExistingRole(TestDataBuilder.CASE_ALLOCATOR_ID,
                                                    "case-allocator",
                                                    RoleCategory.JUDICIAL,
                                                    existingAttributes,
                                                    RoleType.CASE,
                                                    PUBLIC,
                                                    GrantType.STANDARD,
                                                    Status.LIVE
        )));

        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(expectedRoleAssignmentStatus, ra.getStatus()));
    }

    @ParameterizedTest
    @CsvSource({
        "judge,JUDICIAL,judge,SSCS,Benefit,ORGANISATION",
        "hearing-judge,ADMIN,fee-paid-judge,SSCS,Benefit,ORGANISATION",
        "interloc-judge,JUDICIAL,judge,SSCS,Asylum,ORGANISATION",
    })
    void shouldRejectAccessFor_SSCS_CaseRole(String roleName, String roleCategory, String existingRoleName,
                                             String jurisdiction, String caseType, String roleType) {

        HashMap<String, JsonNode> roleAssignmentAttributes = new HashMap<>();
        roleAssignmentAttributes.put("caseId", convertValueJsonNode("1212121212121212"));
        roleAssignmentAttributes.put("requestedRole", convertValueJsonNode(roleName));
        roleAssignmentAttributes.put("caseType", convertValueJsonNode(caseType));
        roleAssignmentAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
            "sscs-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            RoleType.CASE,
            roleAssignmentAttributes,
            PUBLIC,
            SPECIFIC,
            Status.CREATE_REQUESTED,
            TestDataBuilder.CLIENT_ID_XUI,
            false,
            "Access required for reasons",
            TestDataBuilder.ACTORID,
            "reference"
        )
            .build();

        setFeatureFlags();

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode(jurisdiction));
        existingAttributes.put("caseType", convertValueJsonNode(caseType));

        executeDroolRules(List.of(TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.CASE_ALLOCATOR_ID,
                                          "case-allocator",
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          PUBLIC,
                                          GrantType.STANDARD,
                                          RoleType.ORGANISATION
                                      ),
                                  TestDataBuilder
                                      .buildExistingRoleForDrools(
                                          TestDataBuilder.ACTORID,
                                          existingRoleName,
                                          RoleCategory.valueOf(roleCategory),
                                          existingAttributes,
                                          PUBLIC,
                                          GrantType.STANDARD,
                                          RoleType.valueOf(roleType)
                                      )
                          )
        );

        assignmentRequest.getRequestedRoles().forEach(roleAssignment ->
                                                          assertEquals(Status.REJECTED, roleAssignment.getStatus()));
    }

    @ParameterizedTest
    @CsvSource({
        "interloc-judge,JUDICIAL,am_role_assignment_service,ORGANISATION",
        "hearing-judge,JUDICIAL,am_org_role_mapping_service,CASE"
    })
    void shouldRejectDeleteRequest_SSCS_CaseRole(String roleName,
                                                 String roleCategory,
                                                 String clientId,
                                                 String roleType) {

        HashMap<String, JsonNode> existingAttributes = new HashMap<>();
        existingAttributes.put("jurisdiction", convertValueJsonNode("SSCS"));
        existingAttributes.put("caseType", convertValueJsonNode("Benefit"));
        existingAttributes.put("caseId", convertValueJsonNode("1212121212121212"));
        existingAttributes.put("requestedRole", convertValueJsonNode(roleName));

        assignmentRequest = TestDataBuilder.buildAssignmentRequestSpecialAccessGrant(
            "delete-access",
            roleName,
            RoleCategory.valueOf(roleCategory),
            RoleType.valueOf(roleType),
            existingAttributes,
            PUBLIC,
            SPECIFIC,
            DELETE_REQUESTED,
            clientId,
            false,
            "Delete required for reasons",
            TestDataBuilder.ACTORID,
            "reference"
        )
            .build();

        setFeatureFlags();

        // NB: Existing roles are missing case-allocator role for CASE_ALLOCATOR_ID user: hence rejected
        executeDroolRules(List.of(buildExistingRole(TestDataBuilder.ACTORID,
                                                    roleName,
                                                    RoleCategory.JUDICIAL,
                                                    existingAttributes,
                                                    RoleType.CASE,
                                                    PUBLIC,
                                                    GrantType.STANDARD,
                                                    Status.LIVE)));

        assignmentRequest.getRequestedRoles().forEach(ra -> assertEquals(Status.DELETE_REJECTED, ra.getStatus()));
    }

    private void setFeatureFlags() {
        List<String> flags = List.of(
            "sscs_wa_1_0",
            "sscs_case_allocator_1_0",
            "all_wa_services_case_allocator_1_0",
            FeatureFlagEnum.PROBATE_WA_1_0.getValue(),
            FeatureFlagEnum.FR_WA_1_0.getValue()
        );

        for (String flag : flags) {
            featureFlags.add(
                FeatureFlag.builder().flagName(flag).status(true).build()
            );
        }
    }
}
