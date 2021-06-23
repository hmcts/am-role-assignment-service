@F-009
Feature: F-009 : Post Role Assignments Advance Query Request

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-183
  Scenario: must successfully receive Role Assignments without specific page number
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-183_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments without specific page number]
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-183_DeleteDataForMultipleRoleAssignments].

  @S-184
  Scenario: must successfully receive Role Assignments with specific page number
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-183_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments with specific page number],
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-183_DeleteDataForMultipleRoleAssignments].

  @S-186
  Scenario: must successfully receive Role Assignments without optional headers
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [does not have size header],
    And the request [does not have sort header],
    And the request [does not have direction header],
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [F-006_DeleteDataForMultipleRoleAssignments].

  @S-187
  Scenario: must successfully receive Role Assignments with optional headers
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-187_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [has size header],
    And the request [has sort header],
    And the request [has direction header],
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-187_DeleteDataForMultipleRoleAssignments].
