@F-013
Feature: F-013 : Create SSCS Role Assignments

  Background:
    #...

  @S-231
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-214_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains caseworker-sscs-judge or caseworker-sscs-judge-feepaid org role as assignee],
    And the request [contains hearing-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-214_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-214_DeleteDataForRoleAssignmentsForRequestedRole].


