@F-001
Feature: F-001 : Create Role Assignments

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-001
  Scenario: must successfully create single Role Assignment with only mandatory fields
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta2 - who invokes the API],
    And a user [Befta1 - who is the actor for requested role],
    And a user [Befta3 - who is the assigner],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment with only mandatory fields],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-002
  Scenario: must successfully create multiple Role Assignments
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-003
  Scenario: must successfully create single Role Assignment with RoleTypeId as case
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And the request [contains RoleTypeId as 'CASE'],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-004
  Scenario: must receive a Reject response when creation of any Role Assignment not successful
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments where one of the role has invalid data],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-005
  Scenario: must receive a Reject response when rule validation failed
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And the request [contains data which is not as per rule validations],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-006
  Scenario: must receive an error response when RoleName not matched
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And the request [contains an invalid RoleName],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-007
  Scenario: must receive an error response when ReplaceExisting is True without Process and Reference
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And the request [contains ReplaceExisting is true and either process or Reference value is missed],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-008
  Scenario: must receive an error response when BeginTime is less than current time
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And the request [contains BeginTime is less than current time],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-009
  Scenario: must receive an error response when EndTime is less than current time
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And  the request [contains EndTime is less than current time],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-010
  Scenario: must receive an error response when EndTime is less than BeginTime
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And the request [contains EndTime is less than BeginTime],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-011
  Scenario: must successfully create single Role Assignment with RoleTypeId as organisational
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And the request [contains RoleTypeId as organisational],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-012
  Scenario: must successfully create single Role Assignment when ReplaceExisting is True with Process and Reference
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains a single Role Assignment],
    And the request [contains ReplaceExisting is true and have process and Reference values],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-013
  Scenario: must successfully create multiple Role Assignments when ReplaceExisting is True with Process and Reference
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains multiple Role Assignments],
    And the request [contains ReplaceExisting is true and have process and Reference values],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-014
  Scenario: must receive an error response when creation of any Role Assignment is not successful where ReplaceExisting is True
    Given a user with [an active IDAM profile with full permissions],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is true and have process and Reference values],
    And the request [contains multiple Role Assignments where one of the role has invalid data],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected.

  @S-015
  Scenario: must successfully remove single Role Assignment when ReplaceExisting is True along with empty role assignment list
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is true and have process and Reference values],
    And the request [contains an empty Role Assignments list],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response [contains an empty Role Assignments list],
    And the response has all other details as expected.

  @S-016
  Scenario: must successfully receive a positive response when creating same assignment record twice with ReplaceExisting set to True
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [CreationDataForRoleAssignment],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is true and have process and Reference values],
    And the request [contains the same create assignment request executed above],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-017
  Scenario: must successfully receive a positive response when creating mix and match role assignments ReplaceExisting set to True
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-017_Multiple_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is true and have process and Reference values],
    And the request [contains multiple Role Assignments just created and couple of new role assignments],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.

  @S-018
  Scenario: must successfully receive a positive response when existing role assignments replaced with none ReplaceExisting set to True
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create a role assignment for an actor] as in [S-018_Multiple_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains ReplaceExisting is true and have process and Reference values],
    And the request [contains multiple Role Assignments just created and has no new role assignments],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected.
