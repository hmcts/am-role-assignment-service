@F-006
Feature: F-006 : Post Role Assignments Query Request

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-081 @FeatureToggle(search-by-query) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully receive multiple Role Assignments with one query param
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments with one query param]
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  @S-082 @FeatureToggle(search-by-query) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully receive multiple Role Assignments with more than one query params
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments with more than one query params]
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  @S-083 @FeatureToggle(search-by-query) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully receive multiple Role Assignments without specific page number
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-083_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments without specific page number]
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  @S-084 @FeatureToggle(search-by-query) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully receive multiple Role Assignments with specific page number
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-083_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments with specific page number],
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

#
#  @S-064 @FeatureToggle(get-assignments-by-query-params) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
#  Scenario: must successfully receive multiple Role Assignments by Role Type and Actor Id
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to create multiple role assignments for an actor] as in [S-064_CreationDataForRoleAssignment],
#    When a request is prepared with appropriate values,
#    And the request [contains multiple Role Assignments],
#    And the request [contains Role Type as Case and existing Actor Id],
#    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [F-006_DeleteRoleAssignmentsByActorId].
#
#  @S-065 @FeatureToggle(get-assignments-by-query-params) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
#  Scenario: must successfully receive multiple Role Assignments by Role Type and Case Id
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to create multiple role assignments having same CaseId] as in [S-065_CreationDataForRoleAssignment],
#    When a request is prepared with appropriate values,
#    And the request [contains multiple Role Assignments],
#    And the request [contains Role Type as Case and existing CaseId],
#    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [S-065_DeleteRoleAssignmentsByCaseId].
#
#  @S-066 @FeatureToggle(get-assignments-by-query-params)
#  Scenario: must receive an error response for non-existing Actor Id
#    Given a user with [an active IDAM profile with full permissions],
#    When a request is prepared with appropriate values,
#    And the request [contains a non-existing Actor Id],
#    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-067 @FeatureToggle(get-assignments-by-query-params)
#  Scenario: must receive an error response for non-existing Case Id
#    Given a user with [an active IDAM profile with full permissions],
#    When a request is prepared with appropriate values,
#    And the request [contains a non-existing Case Id],
#    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-068 @FeatureToggle(get-assignments-by-query-params) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
#  Scenario: must successfully receive Role Assignments without X-Correlation-ID Header
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to create a role assignment for an actor] as in [CreationDataForRoleAssignment],
#    When a request is prepared with appropriate values,
#    And the request [does not have X-Correlation-ID header],
#    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [F-006_DeleteRoleAssignmentsByActorId].
#
#  @S-069 @FeatureToggle(get-assignments-by-query-params) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
#  Scenario: must successfully receive Role Assignments by both Actor Id and Case Id
#    Given a user with [an active IDAM profile with full permissions],
#    And a successful call [to create a role assignment for an actor] as in [F-006_CreationDataForRoleAssignment],
#    When a request is prepared with appropriate values,
#    And the request [contains both Actor Id and Case Id which are assigned to Role Assignments],
#    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [F-006_DeleteRoleAssignmentsByActorId].
