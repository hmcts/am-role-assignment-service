@F-000
Feature: F-000 : Access Role Assignment API

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-000
  @FeatureToggle(get-ld-flag)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-001
  @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,true)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-002
  @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,true) @ExternalFlagWithExpectedValue(iac_1_0,true)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-003
  @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,false) @ExternalFlagWithExpectedValue(iac_1_0,true)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-004
  @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,true) @ExternalFlagWithExpectedValue(iac_1_0,false)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-005
  @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,false) @ExternalFlagWithExpectedValue(iac_1_0,false)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-006
  @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,false)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-007
  @FeatureToggle(get-ld-flag) @ExternalFlagWithExpectedValue(iac_1_0,false)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.
