Feature: Retry Handling

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @retry-handling
  Scenario: Exponential backoff retry on server error
    Given I have a client configured with retry policy:
      | max_retries | 3 |
      | backoff     | exponential |
    And I set the request header "X-Test-Scenario" to "server_error_then_success"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been retried 2 times
    And the retry delays should follow exponential backoff

  @retry-handling
  Scenario: Retry with Retry-After header
    Given I have a client configured with retry policy:
      | max_retries | 2 |
      | respect_retry_after | true |
    And I set the request header "X-Test-Scenario" to "rate_limit_with_retry_after"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been retried 1 time
    And the retry should have respected the Retry-After header

  @retry-handling
  Scenario: Max retries exceeded
    Given I have a client configured with retry policy:
      | max_retries | 2 |
      | backoff     | exponential |
    And I set the request header "X-Test-Scenario" to "persistent_server_error"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the request should have been retried 2 times
    And the final error should indicate max retries exceeded

  @retry-handling
  Scenario: No retry on client errors
    Given I have a client configured with retry policy:
      | max_retries | 3 |
      | backoff     | exponential |
    When I call Check with:
      | user     |               |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 400
    And the request should not have been retried

  @retry-handling
  Scenario: Custom retry conditions
    Given I have a client configured with retry policy:
      | max_retries | 3 |
      | retry_on_status | [500, 502, 503] |
    And I set the request header "X-Test-Scenario" to "custom_error_502"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been retried based on custom conditions

  @retry-handling
  Scenario: Linear backoff retry policy
    Given I have a client configured with retry policy:
      | max_retries | 3 |
      | backoff     | linear |
      | base_delay  | 100   |
    And I set the request header "X-Test-Scenario" to "transient_error_linear_backoff"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been retried 3 times
    And the retry delays should be linear with base delay 100ms

  @retry-handling
  Scenario: Retry with jitter to prevent thundering herd
    Given I have a client configured with retry policy:
      | max_retries   | 3    |
      | backoff       | exponential |
      | jitter_enabled| true |
    And I set the request header "X-Test-Scenario" to "jitter_test"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been retried 3 times
    And the retry delays should include random jitter

  @retry-handling
  Scenario: Circuit breaker behavior
    Given I have a client configured with retry policy:
      | max_retries              | 3    |
      | circuit_breaker_enabled  | true |
      | circuit_breaker_threshold| 5    |
    And I set the request header "X-Test-Scenario" to "circuit_breaker_test"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the circuit breaker should be triggered after 5 failures
    And subsequent requests should fail fast without retries
