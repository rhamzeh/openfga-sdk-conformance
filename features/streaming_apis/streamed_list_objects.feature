Feature: StreamedListObjects API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @streamed-list-objects
  Scenario: Basic StreamedListObjects request
    When I call StreamedListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should be successful
    And the response should stream objects
    And the objects should include "document:doc1"

  @streamed-list-objects
  Scenario: StreamedListObjects with contextual tuples
    Given I set contextual tuples:
      | user     | relation | object        |
      | user:bob | editor   | document:doc2 |
    When I call StreamedListObjects with:
      | user     | user:bob |
      | relation | viewer   |
      | type     | document |
    Then the response should be successful
    And the response should stream objects

  @streamed-list-objects
  Scenario: StreamedListObjects with context object
    Given I set context object:
      | key        | value   |
      | ip_address | 1.1.1.1 |
    When I call StreamedListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should be successful
    And the response should stream objects

  @streamed-list-objects
  Scenario: StreamedListObjects with authorization model ID
    Given I set authorization model ID to "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call StreamedListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should be successful
    And the response should stream objects

  @streamed-list-objects
  Scenario: StreamedListObjects streaming behavior
    When I call StreamedListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    Then the response should be successful
    And the response should stream objects incrementally
    And each streamed object should be valid

  @streamed-list-objects
  Scenario: StreamedListObjects connection handling
    When I call StreamedListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    And I set the request header "X-Test-Scenario" to "streaming_connection_test"
    Then the streaming response should be successful
    And the streaming connection should be properly closed

  @streamed-list-objects
  Scenario: StreamedListObjects with nested context object
    Given I set the context object:
      | key             | value        |
      | user.department | engineering  |
      | user.level      | senior       |
    When I call StreamedListObjects with:
      | user     | user:david |
      | relation | viewer     |
      | type     | document   |
    Then the streaming response should be successful
    And the streaming response should contain objects
