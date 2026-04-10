@F-021
@FeatureToggle(EV:AZURE_CASE_VALIDATION_FTA_ENABLED=on)
Feature: F-021 : Create Case Role Assignments for POSSESSIONS JUDICIAL case roles

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-021.01a
  Scenario: must successfully create allocated-judge POSSESSIONS case role with existing org role judge
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-021.01a_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-021.01a_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-021.01a_DeleteDataForRoleAssignmentsForOrgRoles].

