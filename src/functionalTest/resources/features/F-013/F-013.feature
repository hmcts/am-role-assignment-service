@F-013
Feature: F-013 : Create Judicial Role Assignments for CIVIL
  Background:
    Given an appropriate test context as detailed in the test data source

  @S-216
  Scenario: must successfully create CIVIL Judge ORG role assignment
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-216_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains challenged-access-legal-operations case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [S-216_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-217
  Scenario: must successfully create CIVIL Circuit Judge ORG role assignment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-217_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains an Actor Id having multiple Role Assignments],
    And it is submitted to call the [Get Role Assignments by Actor Id] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-217_DeleteDataForForRoleAssignmentsForOrgRoles].

  @S-218
  Scenario: must successfully create CIVIL Leadership Judge ORG role assignment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-218_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains an Actor Id having multiple Role Assignments],
    And it is submitted to call the [Get Role Assignments by Actor Id] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-218_DeleteDataForForRoleAssignmentsForOrgRoles].

  @S-219
  Scenario: must successfully create CIVIL Leadership Judge ORG role assignment
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-219_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains an Actor Id having multiple Role Assignments],
    And it is submitted to call the [Get Role Assignments by Actor Id] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-219_DeleteDataForForRoleAssignmentsForOrgRoles].
