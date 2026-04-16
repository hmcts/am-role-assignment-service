@F-027
@FeatureToggle(EV:AZURE_CASE_VALIDATION_FTA_ENABLED=on)
Feature: F-027 : AAC Manage Case Assignment deletion tests

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-027.01
  Scenario: must successfully allow AAC Manage Case Assignment to delete CITIZEN case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create a case-role assignment for a Citizen] as in [S-027.01_CreateCaseRole_CITIZEN],
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for AAC Manage Case Assignment service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to check no case role assignments are found for the actor] as in [F-027_CheckCaseRole__NotFound].

  @S-027.02
  Scenario: must successfully allow AAC Manage Case Assignment to delete PROFESSIONAL case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create a case-role assignment for a Professional] as in [S-027.02_CreateCaseRole_PROFESSIONAL],
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for AAC Manage Case Assignment service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to check no case role assignments are found for the actor] as in [F-027_CheckCaseRole__NotFound].

  @S-027.03
  Scenario: must reject call from AAC Manage Case Assignment to delete ADMIN case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (ADMIN) & requester (case-allocator)] as in [S-027.03_CreateOrgRoles_ADMIN],
    And a successful call [to create a case-role assignment for an Admin] as in [S-027.03_CreateCaseRole_ADMIN],
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for AAC Manage Case Assignment service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected,
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    And a successful call [to delete case role assignments just created above] as in [F-027_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-027_DeleteOrgRoles].

  @S-027.04
  Scenario: must reject call from AAC Manage Case Assignment to delete CTSC case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (CTSC) & requester (case-allocator)] as in [S-027.04_CreateOrgRoles_CTSC],
    And a successful call [to create a case-role assignment for a CTSC] as in [S-027.04_CreateCaseRole_CTSC],
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for AAC Manage Case Assignment service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected,
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    And a successful call [to delete case role assignments just created above] as in [F-027_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-027_DeleteOrgRoles].

  @S-027.05
  Scenario: must reject call from AAC Manage Case Assignment to delete LEGAL_OPS case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (LEGAL_OPS) & requester (case-allocator)] as in [S-027.05_CreateOrgRoles_LEGAL_OPS],
    And a successful call [to create a case-role assignment for a LEGAL_OPS] as in [S-027.05_CreateCaseRole_LEGAL_OPS],
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for AAC Manage Case Assignment service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected,
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    And a successful call [to delete case role assignments just created above] as in [F-027_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-027_DeleteOrgRoles].

  @S-027.06
  Scenario: must reject call from AAC Manage Case Assignment to delete JUDICIAL case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (JUDICIAL) & requester (case-allocator)] as in [S-027.06_CreateOrgRoles_JUDICIAL],
    And a successful call [to create a case-role assignment for JUDICIAL] as in [S-027.06_CreateCaseRole_JUDICIAL],
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for AAC Manage Case Assignment service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected,
    And a successful call [to check case role assignments are found for the actor] as in [F-027_CheckCaseRole__Found],
    And a successful call [to delete case role assignments just created above] as in [F-027_DeleteCaseRoles],
    And a successful call [to delete org role assignments just created above] as in [F-027_DeleteOrgRoles].
