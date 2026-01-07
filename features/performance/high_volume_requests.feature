Feature: High Volume Requests

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @performance @high-volume
  Scenario: High volume Check requests
    When I make 100 concurrent Check requests with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then all responses should be successful
    And the average response time should be under 100ms
    And no requests should timeout

  @performance @high-volume
  Scenario: High volume Write requests
    When I make 50 concurrent Write requests with writes:
      | user     | relation | object        |
      | user:bob | editor   | document:doc1 |
    Then all responses should be successful
    And the average response time should be under 200ms
    And no requests should fail due to rate limiting

  @performance @high-volume
  Scenario: High volume BatchCheck requests
    When I make 20 concurrent BatchCheck requests with 10 checks each
    Then all responses should be successful
    And the average response time should be under 500ms
    And all batch results should be complete

  @performance @high-volume
  Scenario: High volume ClientBatchCheck requests
    When I make 20 concurrent ClientBatchCheck requests with 10 checks each
    Then all responses should be successful
    And all checks should be processed in parallel
    And the average response time should be under 300ms
    And each check should have been processed individually

  @performance @high-volume
  Scenario: Mixed API high volume requests
    When I make concurrent requests:
      | api_type    | count | concurrent_checks |
      | Check       | 50    | 1                 |
      | BatchCheck  | 10    | 5                 |
      | ListObjects | 20    | 1                 |
      | Write       | 15    | 1                 |
    Then all responses should be successful
    And the system should handle mixed load efficiently
    And no requests should timeout or fail
