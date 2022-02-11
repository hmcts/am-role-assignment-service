@F-012
Feature: Create Challenge Access Role

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-213
  @FeatureToggle(RAS:iac_challenged_1_0=on)
  Scenario: must successfully create challenged-access-judiciary role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-213_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains specific-access-legal-ops case requested role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-213_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-213_DeleteDataForRoleAssignmentsForRequestedRole].

  @S-214
  @FeatureToggle(RAS:iac_challenged_1_0=on)
  Scenario: must successfully create challenged-access-legal-operations role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-214_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains specific-access-legal-ops case requested role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-214_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-214_DeleteDataForRoleAssignmentsForRequestedRole].

  @S-215
  @FeatureToggle(RAS:iac_challenged_1_0=on)
  Scenario: must successfully create challenged-access-admin role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-215_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains specific-access-legal-ops case requested role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-215_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-215_DeleteDataForRoleAssignmentsForRequestedRole].
