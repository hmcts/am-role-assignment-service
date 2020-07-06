@F-005
Feature: Get Role Assignments by Actor Id

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-051
  Scenario: must successfully receive single Role Assignment by Actor Id
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains an Actor Id assigned to single Role Assignment],
    And it is submitted to call the [Get Role Assignments by Actor Id] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected.
#
#  @S-052
#  Scenario: must successfully receive multiple Role Assignments by Actor Id
#    Given a user with [an active IDAM profile with full permissions],
#    When a request is prepared with appropriate values,
#    And the request [contains an Actor Id assigned to multiple Role Assignments],
#    And it is submitted to call the [Delete Role Assignments by Actor Id] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-053
#  Scenario: must receive an error response for a non-existing ActorId
#    Given a user with [an active IDAM profile with full permissions],
#    When a request is prepared with appropriate values,
#    And the request [contains a non-existing Actor Id],
#    And it is submitted to call the [Get Role Assignments by Actor Id] operation of [Role Assignment Service],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-054
#  Scenario: must receive an error response when content-type other than application/json
#    Given a user with [an active IDAM profile with full permissions],
#    When a request is prepared with appropriate values,
#    And the request [contains content-type of application/xml],
#    And it is submitted to call the [Get Role Assignments by Actor Id] operation of [Role Assignment Service],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-055
#  Scenario: must successfully receive Role Assignments without X-Correlation-ID Header
#    Given a user with [an active IDAM profile with full permissions],
#    When a request is prepared with appropriate values,
#    And the request [does not have X-Correlation-ID header],
#    And the request [contains an Actor Id assigned to single Role Assignment],
#    And it is submitted to call the [Get Role Assignments by Actor Id] operation of [Role Assignment Service],
#    Then a positive response is received,
#    And the response has all other details as expected.
