Feature: Expand API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @expand
  Scenario: Basic Expand request
    When I call Expand with:
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should contain an expand tree
    And the tree should have a root node

  @expand
  Scenario: Expand request with contextual tuples
    Given I set contextual tuples:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    When I call Expand with:
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should contain an expand tree
    And the contextual tuples should be applied to the expansion
    And the tree should include relationships from contextual tuples

  @expand
  Scenario: Expand with multiple contextual tuples
    Given I set contextual tuples:
      | user       | relation | object        |
      | user:alice | viewer   | document:doc1 |
      | user:bob   | editor   | document:doc1 |
      | user:carol | admin    | document:doc1 |
    When I call Expand with:
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should contain an expand tree
    And the contextual tuples should be applied to the expansion
    And the tree should include all contextual relationships

  @expand
  Scenario: Expand with contextual tuples and context object
    Given I set contextual tuples:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    And I set context object:
      | key        | value   |
      | ip_address | 1.1.1.1 |
      | department | eng     |
    When I call Expand with:
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should contain an expand tree
    And the contextual tuples should be applied to the expansion
    And the context object should be applied to the expansion

  @expand
  Scenario: Expand with contextual tuples affecting computed usersets
    Given I set contextual tuples:
      | user       | relation | object          |
      | user:alice | member   | group:engineers |
      | group:engineers | viewer | document:doc1 |
    When I call Expand with:
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should contain an expand tree
    And the tree should contain computed usersets
    And the contextual tuples should affect computed relationships
    And the tree should show user:alice as having viewer access through group membership

  @expand
  Scenario: Expand request with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call Expand with:
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should contain an expand tree

  @expand
  Scenario: Expand complex relationship
    When I call Expand with:
      | relation | writer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the response should contain an expand tree
    And the tree should contain user nodes
    And the tree should contain computed usersets
