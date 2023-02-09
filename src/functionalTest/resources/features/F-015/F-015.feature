@F-015
Feature: F-015 : Create Specific Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-221
  @FeatureToggle(RAS:iac_specific_1_0=on)
  Scenario: must successfully create specific access requested role for judiciary
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-221_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains specific-access-judiciary case requested role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-221_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-221_DeleteDataForRoleAssignmentsForRequestedRole].

   @S-222
   @FeatureToggle(RAS:iac_specific_1_0=on)
   Scenario: must successfully create specific access requested role for CTSC
     Given a user with [an active IDAM profile with full permissions],
     And a user [Befta1 - who is the actor for requested role],
     And a successful call [to create org role assignments for actors & requester] as in [S-222_Org_Role_Creation],
     When a request is prepared with appropriate values,
     And the request [contains specific-access-ctsc case requested role assignment],
     And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
     Then a positive response is received,
     And the response has all other details as expected.
     And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
     And a successful call [to delete role assignments just created above] as in [S-222_DeleteDataForRoleAssignmentsForOrgRoles],
     And a successful call [to delete role assignments just created above] as in [S-222_DeleteDataForRoleAssignmentsForRequestedRole].

    @S-223
     Scenario: must successfully create allocated-magistrate case role
       Given a user with [an active IDAM profile with full permissions],
       And a user [Befta1 - who is the actor for requested role],
       And a successful call [to create org role assignments for actors & requester] as in [S-223_Org_Role_Creation],
       When a request is prepared with appropriate values,
       And the request [contains ReplaceExisting is false and reference set to caseId],
       And the request [contains allocated-magistrate role assignment],
       And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
       Then a positive response is received,
       #And the response has all other details as expected,
       And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
       And a successful call [to delete role assignments just created above] as in [S-223_DeleteDataForRoleAssignmentsForOrgRoles].

    @S-224
     Scenario: must successfully create hearing-judge case role
       Given a user with [an active IDAM profile with full permissions],
       And a user [Befta1 - who is the actor for requested role],
       And a successful call [to create org role assignments for actors & requester] as in [S-224_Org_Role_Creation],
       When a request is prepared with appropriate values,
       And the request [contains ReplaceExisting is false and reference set to caseId],
       And the request [contains hearing-judge case role assignment],
       And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
       Then a positive response is received,
       #And the response has all other details as expected,
       And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
       And a successful call [to delete role assignments just created above] as in [S-224_DeleteDataForRoleAssignmentsForOrgRoles].




