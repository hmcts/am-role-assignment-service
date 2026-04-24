@F-023
@FeatureToggle(EV:AZURE_CASE_VALIDATION_FTA_ENABLED=on)
Feature: F-023 : Create Case Role Assignments for POSSESSIONS Challenged Access case roles

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-023.03
  Scenario: must successfully create challenged-access-judiciary POSSESSIONS case role with existing org role - hmcts-legal-operations
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-023.03_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains challenged-access-judiciary case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-023.03_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-023.03_DeleteDataForRoleAssignmentsForOrgRoles].

