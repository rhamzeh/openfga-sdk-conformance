Feature: ListRelations API - Client-side convenience method

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @list-relations
  Scenario: ListRelations for user and object
    When I call ListRelations with:
      | user   | user:alice    |
      | object | document:doc1 |
    Then the response should be successful
    And the response should contain relations
    And the relations should include "viewer"

  @list-relations
  Scenario: ListRelations with contextual tuples
    Given I set contextual tuples:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    When I call ListRelations with:
      | user   | user:bob      |
      | object | document:doc1 |
    Then the response should be successful
    And the response should contain relations
    And the relations should include "viewer"
    And the relations should include "editor"

  @list-relations
  Scenario: ListRelations with context object
    Given I set context object:
      | key        | value   |
      | ip_address | 1.1.1.1 |
    When I call ListRelations with:
      | user   | user:alice    |
      | object | document:doc1 |
    Then the response should be successful
    And the response should contain relations

  @list-relations
  Scenario: ListRelations with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call ListRelations with:
      | user   | user:alice    |
      | object | document:doc1 |
    Then the response should be successful
    And the response should contain relations

  @list-relations
  Scenario: ListRelations with no relations
    When I call ListRelations with:
      | user   | user:unknown  |
      | object | document:doc1 |
    Then the response should be successful
    And the response should contain no relations
