Feature: Request Cancellation

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @request-cancellation
  Scenario: Cancel request before completion
    Given I have a client configured with timeout 5000ms
    And I set the request header "X-Test-Scenario" to "slow_response"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And I cancel the request after 1000ms
    Then the request should be cancelled
    And the error should indicate request cancellation

  @request-cancellation
  Scenario: Request timeout handling
    Given I have a client configured with timeout 2000ms
    And I set the request header "X-Test-Scenario" to "timeout_response"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the error should indicate timeout

  @request-cancellation
  Scenario: Graceful cancellation with cleanup
    Given I have a client configured with timeout 5000ms
    And I set the request header "X-Test-Scenario" to "slow_response"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    And I cancel the request gracefully after 1000ms
    Then the request should be cancelled gracefully
    And resources should be cleaned up properly

  @request-cancellation
  Scenario: Multiple concurrent requests with cancellation
    Given I have a client configured with timeout 5000ms
    When I make 3 concurrent Check requests
    And I cancel the second request after 500ms
    Then the first and third requests should complete successfully
    And the second request should be cancelled
    And the error should indicate request cancellation
