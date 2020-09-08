@F-001
Feature: F-001 : Create Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-002 @FeatureToggle(get-ld-flag)
  Scenario: must successfully create multiple Role Assignments
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received


