@F-013
Feature: F-013 : Create SSCS Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-231
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-231_Org_Role_Creation_Multiple],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains judge org role as assignee],
    And the request [contains hearing-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,

@S-232
 @FeatureToggle(RAS:sscs_wa_1_0=on)
 Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
   Given a user with [an active IDAM profile with full permissions],
   And a user [Befta1 - who is the actor for requested role],
   And a successful call [to create org role assignments for actors & requester] as in [S-232_Org_Role_Creation_Multiple],
   When a request is prepared with appropriate values,
   And the request [contains case-allocator org role as assigner],
   And the request [contains fee-paid-medical org role as assignee],
   And the request [contains panel-doctor case role assignment],
   And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
   Then a positive response is received,
   And the response has all other details as expected.

 @S-233
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-233_Org_Role_Creation_Multiple],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains fee-paid-disability org role as assignee],
    And the request [contains panel-disability case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
   And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

 @S-234
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-234_Org_Role_Creation_Multiple],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains fee-paid-financial org role as assignee],
    And the request [contains panel-financial case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

 @S-235
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-235_Org_Role_Creation_Multiple],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains judge org role as assignee],
    And the request [contains panel-appraisal-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-236
    @FeatureToggle(RAS:sscs_wa_1_0=on)
    Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
      Given a user with [an active IDAM profile with full permissions],
      And a user [Befta1 - who is the actor for requested role],
      And a successful call [to create org role assignments for actors & requester] as in [S-236_Org_Role_Creation_Multiple],
      When a request is prepared with appropriate values,
      And the request [contains case-allocator org role as assigner],
      And the request [contains fee-paid-medical org role as assignee],
      And the request [contains panel-appraisal-medical case role assignment],
      And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
      Then a positive response is received,
      And the response has all other details as expected.

 @S-237
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-237_Org_Role_Creation_Multiple],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains judge org role as assignee],
    And the request [contains interloc-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

 @S-238
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-238_Org_Role_Creation_Multiple],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains case-allocator case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
