@F-009
Feature: F-009 : Create Role Assignments for salaried judge

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-120 @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully create single salaried judge Role Assignment
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single salaried judge Role Assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  @S-121 @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully create single Role Assignment with RoleTypeId as case
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single salaried judge Role Assignment],
    And the request [contains RoleTypeId as 'CASE'],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  @S-122 @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully delete single Role Assignment
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single salaried judge Role Assignment],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].
    And a successful call [try to get role assignments just deleted above] as in [S-122_GetDataForRoleAssignments].
