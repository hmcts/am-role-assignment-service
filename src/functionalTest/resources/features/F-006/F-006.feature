@F-006
Feature: Get Role Assignments by Query Params

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-061
  Scenario: must successfully receive Role Assignments by Role Type
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a Role Type which is assigned to Role Assignments],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-062
  Scenario: must receive an error response for a non-existing Role Type
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a non-existing Role Type],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-063
  Scenario: must successfully receive Role Assignments by Role Name
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a Role Name which is assigned to Role Assignments],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-064
  Scenario: must receive an error response for a non-existing Role Name
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a non-existing Role Name],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-065
  Scenario: must successfully receive Role Assignments by Classification Public
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a Classification of PUBLIC],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-066
  Scenario: must successfully receive Role Assignments by Classification Private
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a Classification of PRIVATE],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-067
  Scenario: must successfully receive Role Assignments by Classification Restricted
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a Classification of RESTRICTED],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-068
  Scenario: must receive an error response where Security Classification is less than the required level
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a security classification which is less than the required classification level],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-069
  Scenario: must successfully receive Role Assignments by Active From
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a valid Active From],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-070 #RoleAssignment.Begin <= ActiveFrom
  Scenario: must receive an error response for a improper Active From
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains an improper Active From],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-071
  Scenario: must successfully receive Role Assignments by Active To
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a valid Active To],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-072 #RoleAssignment.End >= ActiveTo
  Scenario: must receive an error response for a improper Active To
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains an improper Active To],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-073
  Scenario: must successfully receive Role Assignments by Active At
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a valid Active At],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-074 #RoleAssignment.Begin <= ActiveAt < RoleAssignment.End
  Scenario: must receive an error response for a improper Active At
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains an improper Active At],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-075
  Scenario: must successfully receive Role Assignments by AttributeName
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains an Attribute Name which is assigned to Role Assignments],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-076
  Scenario: must receive an error response for a improper Active At
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains an Attribute Name which is not assigned to Role Assignments],
    And it is submitted to call the [Get Role Assignments by Query Params] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.
