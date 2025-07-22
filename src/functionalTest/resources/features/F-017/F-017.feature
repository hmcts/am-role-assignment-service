@F-017
Feature: F-017 : Create Role Assignments for Group Access

  @S-250
  Scenario: Invoking POST role-assignments api with request to add a new role assignment
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta6 - who invokes the API],
    When a request is prepared with appropriate values,
    And the request [has roleType Organisation, roleCategory Professional, roleName PRM_Test_GA_Role and attributes has caseAccessGroupId and caseType],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected,
    And a successful call [to delete role assignments just created above] as in [S-250_DeleteDataForRoleAssignmentsForPrmRoles].

  @S-251
  Scenario: Invoking POST role-assignments api with request to add a new role assignment - this is without additional attribute caseAccessGroupId
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta6 - who invokes the API],
    When a request is prepared with appropriate values,
    And the request [has roleType Organisation, roleCategory Professional, roleName PRM_Test_GA_Role and attributes doesn't have caseAccessGroupId],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected

  @S-252
  Scenario: Invoking POST role-assignments api with request to add a new role assignment - this is without additional attribute caseType
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta6 - who invokes the API],
    When a request is prepared with appropriate values,
    And the request [has roleType Organisation, roleCategory Professional, roleName PRM_Test_GA_Role and attributes doesn't have caseType],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected

  @S-253
  Scenario: Invoking POST role-assignments api with request to add a new role assignment but the process does not match 'professional-organisational-role-mapping'
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta6 - who invokes the API],
    When a request is prepared with appropriate values,
    And the request [has roleType Organisation, roleCategory Professional, roleName PRM_Test_GA_Role and process value is not set to 'professional-organisational-role-mapping'],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected

  @S-254
  Scenario: Invoking POST role-assignments api with request to add a new role assignment with not valid classification
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta6 - who invokes the API],
    When a request is prepared with appropriate values,
    And the request [has roleType Organisation, roleCategory Professional, roleName PRM_Test_GA_Role and Classification is not set to Restricted],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected

  @S-255
  Scenario: Invoking POST role-assignments api with request to add a new role assignment with not valid grantType
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta6 - who invokes the API],
    When a request is prepared with appropriate values,
    And the request [has roleType Organisation, roleCategory Professional, roleName PRM_Test_GA_Role and grantType not set to STANDARD],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a negative response is received,
    And the response has all other details as expected

  @S-256
  Scenario: Invoking GET role assignment with valid actorId and returns 200 OK
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta6 - who invokes the API],
    And a successful call [to create org role assignments for actor] as in [S-256_Prm_Role_Creation],
    When a request is prepared with appropriate values,
    And the request [has a actor id passed that does exists],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected
    And a successful call [to delete role assignments just created above] as in [S-256_DeleteDataForRoleAssignmentsForPrmRoles].

  @S-257
  Scenario: Invoking GET role assignment with an invalid actorId and returns 200 OK
    Given an appropriate test context as detailed in the test data source,
    And a user [Befta6 - who invokes the API],
    When a request is prepared with appropriate values,
    And the request [has a actor id passed that does not exists],
    And it is submitted to call the [Create Role Assignments] operation of [Role Assignments Service],
    Then a positive response is received,
    And the response has all other details as expected

