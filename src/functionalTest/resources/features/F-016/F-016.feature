@F-016
Feature: F-016 : Create CIVIL Specific Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-217
  @FeatureToggle(RAS:iac_specific_1_0=on)
  Scenario: must successfully create specific-access-judiciary role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-217_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains specific-access-judiciary case requested role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
   #  Then a positive response is received,
    #  And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [S-217_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-217_DeleteDataForRoleAssignmentsForRequestedRole].

  @S-218
  @FeatureToggle(RAS:iac_specific_1_0=on)
  Scenario: must successfully create specific-access-ctsc role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-218_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains specific-access-ctsc case requested role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
   #  Then a positive response is received,
   #  And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [S-218_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-218_DeleteDataForRoleAssignmentsForRequestedRole].
