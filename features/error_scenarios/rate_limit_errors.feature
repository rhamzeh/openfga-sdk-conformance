Feature: Rate Limit Errors

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @rate-limit-errors
  Scenario: Rate limit error
    Given I set the request header "X-Test-Scenario" to "rate_limit"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 429
    And the error message should contain "rate limit exceeded"

  @rate-limit-errors
  Scenario: Rate limit with retry-after header
    Given I set the request header "X-Test-Scenario" to "rate_limit_with_retry_after"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 429
    And the response should have header "Retry-After"
    And the error message should contain "rate limit exceeded"
