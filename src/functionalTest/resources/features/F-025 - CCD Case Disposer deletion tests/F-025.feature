@F-025
@FeatureToggle(EV:AZURE_CASE_VALIDATION_FTA_ENABLED=on)
Feature: F-025 : CCD Case Disposer deletion tests

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-025.01
  Scenario: must successfully allow case-disposer to delete CITIZEN case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create a case-role assignment for a Citizen] as in [S-025.01_CreateCaseRole_Citizen],
    And a successful call [to check case role assignments are found for the actor] as in [F-025_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for case-disposer service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to check no case role assignments are found for the actor] as in [F-025_CheckCaseRole__NotFound].

  @S-025.02
  Scenario: must successfully allow case-disposer to delete PROFESSIONAL case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create a case-role assignment for a Professional] as in [S-025.02_CreateCaseRole_Professional],
    And a successful call [to check case role assignments are found for the actor] as in [F-025_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for case-disposer service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to check no case role assignments are found for the actor] as in [F-025_CheckCaseRole__NotFound].

  @S-025.03
  @FeatureToggle(RAS:disposer_1_1=on)
  Scenario: must successfully allow case-disposer to delete ADMIN case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (ADMIN) & requester (case-allocator)] as in [S-025.03_CreateOrgRoles_Admin],
    And a successful call [to create a case-role assignment for an Admin] as in [S-025.03_CreateCaseRole_Admin],
    And a successful call [to check case role assignments are found for the actor] as in [F-025_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for case-disposer service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to check no case role assignments are found for the actor] as in [F-025_CheckCaseRole__NotFound],
    And a successful call [to delete org role assignments just created above] as in [F-025_DeleteOrgRoles].

  @S-025.04
  @FeatureToggle(RAS:disposer_1_1=on)
  Scenario: must successfully allow case-disposer to delete CTSC case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (CTSC) & requester (case-allocator)] as in [S-025.04_CreateOrgRoles_CTSC],
    And a successful call [to create a case-role assignment for a CTSC] as in [S-025.04_CreateCaseRole_CTSC],
    And a successful call [to check case role assignments are found for the actor] as in [F-025_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for case-disposer service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to check no case role assignments are found for the actor] as in [F-025_CheckCaseRole__NotFound],
    And a successful call [to delete org role assignments just created above] as in [F-025_DeleteOrgRoles].

  @S-025.05
  @FeatureToggle(RAS:disposer_1_1=on)
  Scenario: must successfully allow case-disposer to delete LEGAL_OPS case-roles
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta3 - who is the actor for requested role],
    And a successful call [to create org role assignments for actor (LEGAL_OPS) & requester (case-allocator)] as in [S-025.05_CreateOrgRoles_LEGAL_OPS],
    And a successful call [to create a case-role assignment for a LEGAL_OPS] as in [S-025.05_CreateCaseRole_LEGAL_OPS],
    And a successful call [to check case role assignments are found for the actor] as in [F-025_CheckCaseRole__Found],
    When a request is prepared with appropriate values,
    And the request [with S2S token for case-disposer service],
    And the request [with process and reference values used to create the case-role assignment],
    And it is submitted to call the [Delete Role Assignments by Process] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to check no case role assignments are found for the actor] as in [F-025_CheckCaseRole__NotFound],
    And a successful call [to delete org role assignments just created above] as in [F-025_DeleteOrgRoles].
