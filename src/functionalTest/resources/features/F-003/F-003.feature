@F-003
Feature: Delete Role Assignments by Role Assignment Id

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-031
  Scenario: must successfully delete single Role Assignment by Role Assignment Id
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a Role Assignment Id],
    And it is submitted to call the [Delete Role Assignments by Role Assignment Id] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-032
  Scenario: must successfully delete Role Assignment without X-Corrlation-ID Header
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [does not have X-Corrlation-ID header],
    And the request [contains a Role Assignment Id],
    And it is submitted to call the [Delete Role Assignments by Role Assignment Id] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-033
  Scenario: must receive an error when delete Role Assignment with a non-existing Role Assignment Id
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a non-existing Role Assignment Id],
    And it is submitted to call the [Delete Role Assignments by Role Assignment Id] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-034
  Scenario: must receive an error when content-type other than application/json
    Given a user with [an active caseworker profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains content-type of application/xml],
    And the request [contains a Role Assignment Id],
    And it is submitted to call the [Delete Role Assignments by Role Assignment Id] operation of [Role Assignment Service],
    Then a negative response is received,
    And the response has all other details as expected.
