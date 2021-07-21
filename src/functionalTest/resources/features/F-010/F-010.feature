@F-010
Feature: F-010 : Post Role Assignments Delete Query Request

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-190
  Scenario: must successfully delete list of multiple queries
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create org role assignments for actors & requester] as in [S-106_Multiple_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains list of multiple search queries],
    And the request [consider the OR operation between search queries],
    And the request [contains Correlation Id],
    And it is submitted to call the [Post Role Assignments Delete Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-106_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-191
  Scenario: must successfully delete single Query Request
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create org role assignments for actors & requester] as in [S-106_Multiple_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains list of single search query],
    And the request [contains Correlation Id],
    And it is submitted to call the [Post Role Assignments Delete Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-106_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-192
  Scenario: must successfully delete role assignments without correlation Id
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create org role assignments for actors & requester] as in [S-106_Multiple_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains list of single search query],
    And the request [does not contain Correlation Id],
    And it is submitted to call the [Post Role Assignments Delete Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-106_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-193
  Scenario: must receive a positive response when trying to delete Role Assignments twice
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create org role assignments for actors & requester] as in [S-106_Multiple_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains list of single search query],
    And it is submitted to call the [Post Role Assignments Delete Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And it is submitted to call the [Post Role Assignments Delete Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-106_DeleteDataForRoleAssignmentsForOrgRoles].

  @S-194
  Scenario: must successfully receive the delete records count in headers
    Given a user with [an active IDAM profile with full permissions],
    And a successful call [to create org role assignments for actors & requester] as in [S-106_Multiple_Org_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [contains list of single search query],
    And it is submitted to call the [Post Role Assignments Delete Query Request] operation of [Role Assignment Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-106_DeleteDataForRoleAssignmentsForOrgRoles].
