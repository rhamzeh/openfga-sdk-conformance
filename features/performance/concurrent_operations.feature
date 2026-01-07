Feature: Concurrent Operations

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @performance @concurrent
  Scenario: Concurrent Check operations
    When I make 20 concurrent Check requests with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then all responses should be successful
    And all requests should complete within 5 seconds
    And no race conditions should occur

  @performance @concurrent
  Scenario: Concurrent Write operations
    When I make 10 concurrent Write requests with different tuples:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
      | user:alice | viewer | document:doc2 |
      | user:carol | admin  | document:doc3 |
    Then all responses should be successful
    And all writes should be processed correctly
    And no data corruption should occur

  @performance @concurrent
  Scenario: Mixed concurrent operations
    When I make concurrent requests:
      | operation   | count | parameters |
      | Check       | 15    | user:alice, viewer, document:doc1 |
      | Write       | 5     | various tuples |
      | ListObjects | 10    | user:alice, viewer, document |
    Then all responses should be successful
    And operations should not interfere with each other
    And performance should remain consistent
