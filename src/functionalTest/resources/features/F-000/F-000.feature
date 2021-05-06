@F-000
Feature: F-000 : Access Role Assignment API

  Background:
    Given an appropriate test context as detailed in the test data source

#  @S-000 @FeatureToggle(get-ld-flag)
#  @S-000 @FeatureToggle(get-ld-flag,# #true)// fetch LD value from server, compare with parameter value. If both values are same, execute scenario
#  @S-000 @FeatureToggle(get-ld-flag,# #falsex)
#    #//Database - @dbFeatureToggle(Database flagName, expectedValue)//
#    #//build API to get flag name
#    #//Befta will call get dbflag API and get the value
#
#    #//@FeatureToggle(get-ld-flag, false)   @dbFeatureToggle(Database flagName, expectedValue)
#    #// If both annotation return true, execute scenario, else Skip.

  #@S-000 @FeatureToggle(get-ld-flag)
  #@S-000 @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,true)
  #@S-000 @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,true) @DatabaseFlagWithExpectedValue(fetchFlagStatus?flagName=iac_1_0&env=pr,iac_1_0,false)

  @S-000 @FeatureToggle(get-ld-flag) @FeatureFlagWithExpectedValue(get-ld-flag,true) @DatabaseFlagWithExpectedValue(fetchFlagStatus?flagName=iac_1_0&env=pr,iac_1_0,true)
  Scenario: must access Role Assignment API
    Given a user with [an active caseworker profile],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Role Assignment API],
    And it is submitted to call the [Access Role Assignment API] operation of [Role Assignment API],
    Then a positive response is received,
    And the response has all other details as expected.
