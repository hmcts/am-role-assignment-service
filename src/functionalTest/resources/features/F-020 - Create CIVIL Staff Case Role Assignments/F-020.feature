@F-020
@FeatureToggle(EV:AZURE_CASE_VALIDATION_FTA_ENABLED=on)
Feature: F-020 : Create Case Role Assignments for CIVIL Staff

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-020.01
  Scenario: must successfully create allocated-legal-adviser CIVIL case role with existing org role tribunal-caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.01_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-legal-adviser case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.01_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.01_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.02
  Scenario: must successfully create allocated-legal-adviser CIVIL case role with existing org role senior-tribunal-caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.02_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-legal-adviser case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.02_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.02_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.03
  Scenario: must successfully create allocated-admin-caseworker CIVIL case role with existing org role hearing-centre-admin
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.03_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-admin-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.03_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.03_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.04
  Scenario: must successfully create allocated-admin-caseworker CIVIL case role with existing org role hearing-centre-team-leader
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.04_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-admin-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.04_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.04_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.05
  Scenario: must successfully create allocated-ctsc-caseworker CIVIL case role with existing org role ctsc
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.05_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-ctsc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.05_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.05_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.06
  Scenario: must successfully create allocated-ctsc-caseworker CIVIL case role with existing org role ctsc-team-leader
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.06_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-ctsc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.06_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.06_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.07
  Scenario: must successfully create allocated-nbc-caseworker CIVIL case role with existing org role national-business-centre
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.07_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-nbc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.07_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.07_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.08
  Scenario: must successfully create allocated-nbc-caseworker CIVIL case role with existing org role nbc-team-leader
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.08_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-nbc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.08_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.08_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.09
  Scenario: must successfully create allocated-legal-adviser GENERALAPPLICATION case role with existing org role tribunal-caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.09_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-legal-adviser case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.09_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.09_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.10
  Scenario: must successfully create allocated-legal-adviser GENERALAPPLICATION case role with existing org role senior-tribunal-caseworker
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.10_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-legal-adviser case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.10_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.10_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.11
  Scenario: must successfully create allocated-admin-caseworker GENERALAPPLICATION case role with existing org role hearing-centre-admin
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.11_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-admin-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.11_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.11_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.12
  Scenario: must successfully create allocated-admin-caseworker GENERALAPPLICATION case role with existing org role hearing-centre-team-leader
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.12_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-admin-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.12_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.12_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.13
  Scenario: must successfully create allocated-ctsc-caseworker GENERALAPPLICATION case role with existing org role ctsc
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.13_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-ctsc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.13_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.13_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.14
  Scenario: must successfully create allocated-ctsc-caseworker GENERALAPPLICATION case role with existing org role ctsc-team-leader
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.14_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-ctsc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.14_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.14_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.15
  Scenario: must successfully create allocated-nbc-caseworker GENERALAPPLICATION case role with existing org role national-business-centre
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.15_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-nbc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.15_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.15_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-020.16
  Scenario: must successfully create allocated-nbc-caseworker GENERALAPPLICATION case role with existing org role nbc-team-leader
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-020.16_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains allocated-nbc-caseworker case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete case role assignment for the same actor] as in [S-020.16_Delete_Case_Role],
    And a successful call [to delete role assignments just created above] as in [S-020.16_DeleteDataForRoleAssignmentsForOrgRoles].

