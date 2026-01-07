Feature: BatchCheck API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @batch-check
  Scenario: BatchCheck with multiple checks
    When I call BatchCheck with:
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
      | user:carol | admin  | document:readme |
    Then the response should be successful
    And the response should contain batch check results
    And each check should have a result
    And the first check should be "allowed"
    And the second check should be "allowed"
    And the third check should be "denied"

  @batch-check
  Scenario: BatchCheck with contextual tuples
    Given I set contextual tuples:
      | user     | relation | object          |
      | user:bob | editor   | document:readme |
    When I call BatchCheck with:
      | user:alice | viewer | document:readme |
      | user:bob   | viewer | document:readme |
    Then the response should be successful
    And the response should contain batch check results
    And each check should have a result
    And the first check should be "allowed"
    And the second check should be "allowed"

  @batch-check
  Scenario: BatchCheck with context object
    Given I set context object:
      | key        | value   |
      | ip_address | 1.1.1.1 |
    When I call BatchCheck with:
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
    Then the response should be successful
    And the response should contain batch check results
    And each check should have a result

  @batch-check
  Scenario: BatchCheck with custom headers
    Given I set the request header "X-Custom-Header" to "test-value"
    When I call BatchCheck with:
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
    Then the response should be successful
    And the response should contain batch check results
    And the request should have been made with custom headers

  @batch-check
  Scenario: BatchCheck with mixed results
    When I call BatchCheck with:
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
      | user:carol | admin  | document:readme |
      | user:dave  | owner  | document:readme |
    Then the response should be successful
    And the response should contain batch check results
    And each check should have a result
    And the first check should be "allowed"
    And the second check should be "allowed"
    And the third check should be "denied"

  @batch-check
  Scenario: BatchCheck with correlation IDs
    When I call BatchCheck with correlation IDs:
      | user:alice | viewer | document:readme | check-alice-viewer |
      | user:bob   | editor | document:readme | check-bob-editor   |
      | user:carol | admin  | document:readme | check-carol-admin  |
    Then the response should be successful
    And the response should contain batch check results
    And each result should have a correlation ID
    And the result with correlation ID "check-alice-viewer" should be "allowed"
    And the result with correlation ID "check-bob-editor" should be "allowed"
    And the result with correlation ID "check-carol-admin" should be "denied"

  @batch-check
  Scenario: BatchCheck with duplicate requests (deduplication)
    When I call BatchCheck with:
      | user:alice | viewer | document:readme |
      | user:alice | viewer | document:readme |
      | user:bob   | editor | document:readme |
      | user:alice | viewer | document:readme |
    Then the response should be successful
    And the response should contain batch check results
    And duplicate requests should be deduplicated
    And the response should contain 2 unique results

  @batch-check
  Scenario: BatchCheck with large batch (50+ items)
    When I call BatchCheck with 55 permission checks
    Then the response should be successful
    And the response should contain batch check results
    And the response should contain 55 results
    And the SDK should handle batch size limits automatically

  @batch-check
  Scenario: BatchCheck with invalid correlation IDs
    When I call BatchCheck with invalid correlation IDs:
      | user:alice | viewer | document:readme | invalid-correlation-id-that-is-way-too-long-and-exceeds-the-36-character-limit |
    Then the response should be an error
    And the error should indicate invalid correlation ID format

  @batch-check
  Scenario: BatchCheck with duplicate correlation IDs
    When I call BatchCheck with duplicate correlation IDs:
      | user:alice | viewer | document:readme | duplicate-id |
      | user:bob   | editor | document:readme | duplicate-id |
    Then the response should be an error
    And the error should indicate duplicate correlation IDs
