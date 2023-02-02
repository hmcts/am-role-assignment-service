@F-013
Feature: F-013 : Create Specific and Challenged Access Role

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-216
  @FeatureToggle(RAS:civil_challenged_1_0=on)
  Scenario: must successfully create challenged-access-judiciary role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-216_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains challenged-access-legal-operations case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [S-216_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-216_DeleteDataForRoleAssignmentsForChallengedAccess].

  @S-217
  @FeatureToggle(RAS:civil_specific_1_0=on)
  Scenario: must successfully create specific-access-judiciary role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-217_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains challenged-access-judiciary case requested role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [S-217_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-217_DeleteDataForRoleAssignmentsForChallengedAccess].

  @S-218
  @FeatureToggle(RAS:civil_specific_1_0=on)
  Scenario: must successfully create specific-access-ctsc role
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-218_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains challenged-access-admin case requested role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [S-218_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-218_DeleteDataForRoleAssignmentsForChallengedAccess].
