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

  @S-085 @FeatureToggle(search-by-query) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully receive Role Assignments without X-Correlation-ID Header
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-083_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [does not have X-Correlation-ID header],
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  @S-086 @FeatureToggle(search-by-query) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
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
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].

  @S-087 @FeatureToggle(search-by-query) @FeatureToggle(create-role-assignments) @FeatureToggle(delete-role-assignments)
  Scenario: must successfully receive Role Assignments with optional headers
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-083_CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [has size header],
    And the request [has sort header],
    And the request [has direction header],
    And it is submitted to call the [Post Role Assignments Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
#    And a successful call [to delete role assignments just created above] as in [DeleteDataForRoleAssignments].
