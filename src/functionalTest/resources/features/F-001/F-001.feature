@F-001
Feature: F-001 : Create Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

#  @S-001
#  Scenario: must successfully create single Role Assignment with only mandatory fields
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment with only mandatory fields],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-002
#  Scenario: must successfully create multiple Role Assignments
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains multiple Role Assignments],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-003
#  Scenario: must successfully create single Role Assignment with Role Type ID is case-role
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And the request [contains Role Type Id is case-role],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-012
#  Scenario: must successfully create single Role Assignment with Role Type ID is organisational-role
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And the request [contains Role Type Id is organisational-role],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-013
#  Scenario: must successfully create single Role Assignment when ReplaceExisting is True with Process and Reference
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And the request [contains ReplaceExisting is true and have process and Reference values],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-018
#  Scenario: must successfully create multiple Role Assignments when ReplaceExisting is True with Process and Reference
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains multiple Role Assignments],
#    And the request [contains ReplaceExisting is true and have process and Reference values],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a positive response is received,
#    And the response has all other details as expected.
#
#  @S-019  #Created this scenario from design review. Need to check with Nitin/Aashish
#  Scenario: must receive an error response when creation of any Role Assignment is not successful where ReplaceExisting is True
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains ReplaceExisting is true and have process and Reference values],
#    And the request [contains multiple Role Assignments where one of the role has invalid data],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-004
#  Scenario: must receive an error response when creation of any Role Assignment not successful
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains multiple Role Assignments where one of the role has invalid data],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-005
#  Scenario: must receive an error response when rule validation failed
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains multiple Role Assignments],
#    And the request [contains data which is not as per rule validations],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-006
#  Scenario: must receive an error response when RoleName not matched
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And the request [contains an invalid RoleName],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-007
#  Scenario: must receive an error response when ReplaceExisting is True without Process and Reference
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And the request [contains ReplaceExisting is true and  either process or Reference value is missed],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-008
#  Scenario: must receive an error response when BeginTime is less than current time
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And And the request [contains BeginTime less than current time],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-009
#  Scenario: must receive an error response when EndTime is less than current time
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And And the request [contains EndTime less than current time],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-010
#  Scenario: must receive an error response when EndTime is less than BeginTime
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And the request [contains EndTime less than BeginTime],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a negative response is received,
#    And the response has all other details as expected.
#
#  @S-011
#  Scenario: must successfully removed expired records from LIVE Table
#    Given a user with [an active caseworker profile with full permissions],
#    And a successful call [to create a token for role creation] as in [Befta_Jurisdiction2_Default_Token_Creation_Data_For_Role_Assignment]
#    When a request is prepared with appropriate values,
#    And the request [contains a single Role Assignment],
#    And the request [contains EndTime about to expire],
#    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments API],
#    Then a positive response is received,
#    And the response has all other details as expected.
#    And a call [to retrieve just expired RoleAssignments by Get RoleAssignments] will get the expected response as in [XXXX_Get_RoleAssignemnt_Data_Base].

