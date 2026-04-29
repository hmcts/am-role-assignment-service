@F-023
@FeatureToggle(EV:AZURE_CASE_VALIDATION_FTA_ENABLED=on)
Feature: F-023 : Create Case Role Assignments for POSSESSIONS Challenged Access case roles

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-023.01
  Scenario: must successfully create challenged-access-judiciary POSSESSIONS case role with existing judicial org role
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create JUDICIAL org role assignments for actor] as in [S-023.01_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains challenged-access-judiciary case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignments just created above] as in [F-023_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-023_DeleteOrgRoles].

  @S-023.02
  Scenario: must successfully create challenged-access-admin POSSESSIONS case role with existing admin org role
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create ADMIN org role assignments for actor] as in [S-023.02_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains challenged-access-admin case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignments just created above] as in [F-023_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-023_DeleteOrgRoles].

  @S-023.03
  Scenario: must successfully create challenged-access-ctsc POSSESSIONS case role with existing ctsc org role
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create CTSC org role assignments for actor] as in [S-023.03_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains challenged-access-ctsc case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignments just created above] as in [F-023_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-023_DeleteOrgRoles].
