Feature: ListUsers API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @list-users
  Scenario: Basic ListUsers request
    When I call ListUsers with:
      | object   | document:doc1 |
      | relation | viewer        |
      | type     | user          |
    Then the response should be successful
    And the response should contain users
    And the users should include "user:alice"

  @list-users
  Scenario: ListUsers with contextual tuples
    Given I set contextual tuples:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    When I call ListUsers with:
      | object   | document:doc1 |
      | relation | viewer        |
      | type     | user          |
    Then the response should be successful
    And the response should contain users

  @list-users
  Scenario: ListUsers with context object
    Given I set context object:
      | key        | value   |
      | ip_address | 1.1.1.1 |
    When I call ListUsers with:
      | object   | document:doc1 |
      | relation | viewer        |
      | type     | user          |
    Then the response should be successful
    And the response should contain users

  @list-users
  Scenario: ListUsers with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call ListUsers with:
      | object   | document:doc1 |
      | relation | viewer        |
      | type     | user          |
    Then the response should be successful
    And the response should contain users

  @list-users
  Scenario: ListUsers with user filters
    When I call ListUsers with:
      | object   | document:doc1 |
      | relation | viewer        |
      | type     | user          |
      | filter   | user:*        |
    Then the response should be successful
    And the response should contain users

  @list-users
  Scenario: ListUsers with no results
    When I call ListUsers with:
      | object   | document:unknown |
      | relation | viewer           |
      | type     | user             |
    Then the response should be successful
    And the response should contain no users
