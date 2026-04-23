@F-022
@FeatureToggle(EV:AZURE_CASE_VALIDATION_FTA_ENABLED=on)
Feature: F-022 : Create Case Role Assignments for POSSESSIONS STAFF case roles

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-022.07
  Scenario: must successfully create allocated-ctsc-caseworker POSSESSIONS case role with existing org role - ctsc
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-022.07_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is false and reference set to caseId],
    And the request [contains allocated-ctsc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-022.07_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-022.07_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.07a
   Scenario: must successfully create allocated-ctsc-caseworker POSSESSIONS case role with existing org role - ctsc-team-leader
     Given a user with [an active IDAM profile with full permissions],
     And a user [Befta3 - who is the actor for requested role],
     And a successful call [to create org role assignments for actors & requester] as in [S-022.07a_Org_Role_Creation],
     When a request is prepared with appropriate values,
     And the request [contains ReplaceExisting is false and reference set to caseId],
     And the request [contains allocated-ctsc-caseworker case role assignment],
     And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
     Then a positive response is received,
     And the response has all other details as expected,
     And a successful call [to delete case role assignment for the same actor] as in [S-022.07_Delete_Case_Role],
     And a successful call [to delete role assignments just created above] as in [S-022.07a_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.08
  Scenario: must successfully create allocated-admin-caseworker POSSESSIONS case role with existing org role  - hearing-centre-admin
     Given a user with [an active IDAM profile with full permissions],
     And a user [Befta3 - who is the actor for requested role],
     And a successful call [to create org role assignments for actors & requester] as in [S-022.08_Org_Role_Creation],
     When a request is prepared with appropriate values,
     And the request [contains ReplaceExisting is false and reference set to caseId],
     And the request [contains hearing-centre-admin case role assignment],
     And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
     Then a positive response is received,
     And the response has all other details as expected,
     And a successful call [to delete case role assignment for the same actor] as in [S-022.08_Delete_Case_Role],
     And a successful call [to delete role assignments just created above] as in [S-022.08_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.08a
  Scenario: must successfully create allocated-admin-caseworker POSSESSIONS case role with existing org role  - hearing-centre-team-leader
     Given a user with [an active IDAM profile with full permissions],
     And a user [Befta3 - who is the actor for requested role],
     And a successful call [to create org role assignments for actors & requester] as in [S-022.08a_Org_Role_Creation],
     When a request is prepared with appropriate values,
     And the request [contains ReplaceExisting is false and reference set to caseId],
     And the request [contains hearing-centre-team-leader case role assignment],
     And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
     Then a positive response is received,
     And the response has all other details as expected,
     And a successful call [to delete case role assignment for the same actor] as in [S-022.08_Delete_Case_Role],
     And a successful call [to delete role assignments just created above] as in [S-022.08a_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.9
  Scenario: must successfully create allocated-wlu-caseworker POSSESSIONS case role with existing org role - wlu-admin
     Given a user with [an active IDAM profile with full permissions],
     And a user [Befta3 - who is the actor for requested role],
     And a successful call [to create org role assignments for actors & requester] as in [S-022.09_Org_Role_Creation],
     When a request is prepared with appropriate values,
     And the request [contains ReplaceExisting is false and reference set to caseId],
     And the request [contains wlu-admin case role assignment],
     And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
     Then a positive response is received,
     And the response has all other details as expected,
     And a successful call [to delete case role assignment for the same actor] as in [S-022.09_Delete_Case_Role],
     And a successful call [to delete role assignments just created above] as in [S-022.09_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.09a
  Scenario: must successfully create allocated-wlu-caseworker POSSESSIONS case role with existing org role - wlu-team-leader
      Given a user with [an active IDAM profile with full permissions],
      And a user [Befta3 - who is the actor for requested role],
      And a successful call [to create org role assignments for actors & requester] as in [S-022.09a_Org_Role_Creation],
      When a request is prepared with appropriate values,
      And the request [contains ReplaceExisting is false and reference set to caseId],
      And the request [contains wlu-team-leader case role assignment],
      And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
      Then a positive response is received,
      And the response has all other details as expected,
      And a successful call [to delete case role assignment for the same actor] as in [S-022.09_Delete_Case_Role],
      And a successful call [to delete role assignments just created above] as in [S-022.09a_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.10 @Ignore
  # Bailiff role is not yet added to the system, so ignoring the test for now.
      Scenario: must successfully create allocated-bailiff POSSESSIONS case role with existing org role - bailiff
        Given a user with [an active IDAM profile with full permissions],
        And a user [Befta3 - who is the actor for requested role],
        And a successful call [to create org role assignments for actors & requester] as in [S-022.10_Org_Role_Creation],
        When a request is prepared with appropriate values,
        And the request [contains ReplaceExisting is false and reference set to caseId],
        And the request [contains bailiff case role assignment],
        And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
        Then a positive response is received,
        And the response has all other details as expected,
        And a successful call [to delete case role assignment for the same actor] as in [S-022.10_Delete_Case_Role],
        And a successful call [to delete role assignments just created above] as in [S-022.10_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.10a @Ignore
  Scenario: must successfully create allocated-bailiff POSSESSIONS case role with existing org role - bailiff-manager
      Given a user with [an active IDAM profile with full permissions],
      And a user [Befta3 - who is the actor for requested role],
      And a successful call [to create org role assignments for actors & requester] as in [S-022.10a_Org_Role_Creation],
      When a request is prepared with appropriate values,
      And the request [contains ReplaceExisting is false and reference set to caseId],
      And the request [contains bailiff-manager case role assignment],
      And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
      Then a positive response is received,
      And the response has all other details as expected,
      And a successful call [to delete case role assignment for the same actor] as in [S-022.10_Delete_Case_Role],
      And a successful call [to delete role assignments just created above] as in [S-022.10a_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.11 @Ignore
  Scenario: must successfully create case-allocator POSSESSIONS case case-allocator Enforcement role
      Given a user with [an active IDAM profile with full permissions],
      And a user [Befta3 - who is the actor for requested role],
      And a successful call [to create org role assignments for actors & requester] as in [S-022.11_Org_Role_Creation],
      When a request is prepared with appropriate values,
      And the request [contains ReplaceExisting is false and reference set to caseId],
      And the request [contains case-allocator case role assignment],
      And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
      Then a positive response is received,
      And the response has all other details as expected,
      And a successful call [to delete case role assignment for the same actor] as in [S-022.11_Delete_Case_Role],
      And a successful call [to delete role assignments just created above] as in [S-022.11_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.11a
  Scenario: must successfully create case-allocator POSSESSIONS case case-allocator CTSC role
      Given a user with [an active IDAM profile with full permissions],
      And a user [Befta3 - who is the actor for requested role],
      And a successful call [to create org role assignments for actors & requester] as in [S-022.11a_Org_Role_Creation],
      When a request is prepared with appropriate values,
      And the request [contains ReplaceExisting is false and reference set to caseId],
      And the request [contains case-allocator case role assignment],
      And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
      Then a positive response is received,
      And the response has all other details as expected,
      And a successful call [to delete case role assignment for the same actor] as in [S-022.11_Delete_Case_Role],
      And a successful call [to delete role assignments just created above] as in [S-022.11a_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-022.11b
  Scenario: must successfully create case-allocator POSSESSIONS case case-allocator ADMIN role
     Given a user with [an active IDAM profile with full permissions],
     And a user [Befta3 - who is the actor for requested role],
     And a successful call [to create org role assignments for actors & requester] as in [S-022.11b_Org_Role_Creation],
     When a request is prepared with appropriate values,
     And the request [contains ReplaceExisting is false and reference set to caseId],
     And the request [contains case-allocator case role assignment],
     And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
     Then a positive response is received,
     And the response has all other details as expected,
     And a successful call [to delete case role assignment for the same actor] as in [S-022.11_Delete_Case_Role],
     And a successful call [to delete role assignments just created above] as in [S-022.11b_DeleteDataForRoleAssignmentsForOrgRoles].
