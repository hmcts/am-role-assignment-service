@F-013
Feature: F-013 : Create SSCS Role Assignments

  Background:
    #New Case Role FTAs

  @S-231
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-231_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains caseworker-sscs-judge or caseworker-sscs-judge-feepaid org role as assignee],
    And the request [contains hearing-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-231_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-231_DeleteDataForRoleAssignmentsForRequestedRole].

 @S-232
 @FeatureToggle(RAS:sscs_wa_1_0=on)
 Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
   Given a user with [an active IDAM profile with full permissions],
   And a user [Befta1 - who is the actor for requested role],
   And a successful call [to create org role assignments for actors & requester] as in [S-232_Org_Role_Creation],
   When a request is prepared with appropriate values,
   And the request [contains case-allocator org role as assigner],
   And the request [contains caseworker-sscs-medical or caseworker-sscs-medical-feepaid org role as assignee],
   And the request [contains panel-doctor case role assignment],
   And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
   Then a positive response is received,
   And the response has all other details as expected.
   And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
   And a successful call [to delete role assignments just created above] as in [S-232_DeleteDataForRoleAssignmentsForOrgRoles],
   And a successful call [to delete role assignments just created above] as in [S-232_DeleteDataForRoleAssignmentsForRequestedRole].

 @S-233
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-233_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains caseworker-sscs-disability-feepaid org role as assignee],
    And the request [contains panel-disability case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
   And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
   And a successful call [to delete role assignments just created above] as in [S-233_DeleteDataForRoleAssignmentsForOrgRoles],
   And a successful call [to delete role assignments just created above] as in [S-233_DeleteDataForRoleAssignmentsForRequestedRole].

 @S-234
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-214_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains caseworker-sscs-financial-feepaid org role as assignee],
    And the request [contains panel-financial case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

 @S-235
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-235_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains caseworker-sscs-judge org role as assignee],
    And the request [contains panel-appraisal-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-233_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-233_DeleteDataForRoleAssignmentsForRequestedRole].

@S-236
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-214_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains caseworker-sscs-medical or caseworker-sscs-medical-feepaid org role as assignee],
    And the request [contains panel-appraisal-medical case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-236_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-236_DeleteDataForRoleAssignmentsForRequestedRole].

@S-237
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-237_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains caseworker-sscs-judge org role as assignee],
    And the request [contains interloc-judge case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-237_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-237_DeleteDataForRoleAssignmentsForRequestedRole].

 @S-238
  @FeatureToggle(RAS:sscs_wa_1_0=on)
  Scenario: must successfully create SSCS CASE role assignment with all mandatory fields
    Given a user with [an active IDAM profile with full permissions],
    And a user [Befta1 - who is the actor for requested role],
    And a successful call [to create org role assignments for actors & requester] as in [S-238_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains case-allocator org role as assigner],
    And the request [contains case-allocator org role as assigner],
    And the request [contains case-allocator case role assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments],
    And a successful call [to delete role assignments just created above] as in [S-238_DeleteDataForRoleAssignmentsForOrgRoles],
    And a successful call [to delete role assignments just created above] as in [S-238_DeleteDataForRoleAssignmentsForRequestedRole].
