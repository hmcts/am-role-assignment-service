@F-021
@FeatureToggle(EV:AZURE_CASE_VALIDATION_FTA_ENABLED=on)
Feature: F-021 : Create Case Role Assignments for POSSESSIONS JUDICIAL case roles

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-021.01
  Scenario: must successfully create allocated-judge POSSESSIONS case role with existing org role - judge
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (judge) & requester (case-allocator)] as in [S-021.01_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains allocated-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignments just created above] as in [F-021_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-021_DeleteOrgRoles].

  @S-021.01a
  Scenario: must successfully create allocated-judge POSSESSIONS case role with existing org role - fee-paid-judge
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (fee-paid-judge) & requester (case-allocator)] as in [S-021.01a_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains allocated-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignments just created above] as in [F-021_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-021_DeleteOrgRoles].

  @S-021.03
  Scenario: must successfully create hearing-judge POSSESSIONS case role with existing org role - judge
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (judge) & requester (case-allocator)] as in [S-021.03_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains hearing-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignments just created above] as in [F-021_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-021_DeleteOrgRoles].

  @S-021.03a
  Scenario: must successfully create hearing-judge POSSESSIONS case role with existing org role - fee-paid-judge
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (fee-paid-judge) & requester (case-allocator)] as in [S-021.03a_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains hearing-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignments just created above] as in [F-021_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-021_DeleteOrgRoles].

  @S-021.04
  Scenario: must successfully create case-allocator POSSESSIONS case role with existing org role - case-allocator JUDICIAL role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (case-allocator JUDICIAL) & requester (case-allocator)] as in [S-021.04_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains case-allocator JUDICIAL case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignments just created above] as in [F-021_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-021_DeleteOrgRoles].
