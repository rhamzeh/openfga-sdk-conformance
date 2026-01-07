Feature: ListObjects API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @list-objects
  Scenario: Basic ListObjects request
    When I call ListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should be successful
    And the response should contain objects
    And the objects should include "document:doc1"

  @list-objects
  Scenario: ListObjects with contextual tuples
    Given I set contextual tuples:
      | user     | relation | object        |
      | user:bob | editor   | document:doc2 |
    When I call ListObjects with:
      | user     | user:bob |
      | relation | viewer   |
      | type     | document |
    Then the response should be successful
    And the response should contain objects

  @list-objects
  Scenario: ListObjects with context object
    Given I set context object:
      | key        | value   |
      | ip_address | 1.1.1.1 |
    When I call ListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should be successful
    And the response should contain objects

  @list-objects
  Scenario: ListObjects with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call ListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should be successful
    And the response should contain objects

  @list-objects
  Scenario: ListObjects with higher consistency
    Given I set consistency to "HIGHER_CONSISTENCY"
    When I call ListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should be successful
    And the response should contain objects

  @list-objects
  Scenario: ListObjects with no results
    When I call ListObjects with:
      | user     | user:unknown |
      | relation | viewer       |
      | type     | document     |
    Then the response should be successful
    And the response should contain no objects

  @list-objects
  Scenario: ListObjects with consistency preference
    When I call ListObjects with:
      | user     | user:alice    |
      | relation | viewer        |
      | type     | document      |
    And consistency preference "MINIMIZE_LATENCY"
    Then the response should be successful
    And the response should contain objects

  @list-objects
  Scenario: ListObjects with missing required parameters
    When I call ListObjects with:
      | user | user:alice |
    Then the response should fail
    And the response should have status code 400
    And the error message should contain "relation is required"

  @list-objects
  Scenario: ListObjects with no matching objects
    When I call ListObjects with:
      | user     | user:nonexistent |
      | relation | viewer           |
      | type     | document         |
    Then the response should be successful
    And the response should contain exactly 0 objects
