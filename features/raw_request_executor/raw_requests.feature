Feature: Raw Request Executor

  @raw-request-executor @go-only
  Scenario: Execute raw GET request
    Given I have a client configured with raw request executor
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I execute a raw GET request to "/stores/01ARZ3NDEKTSV4RRFFQ69G5FAV"
    Then the response should be successful
    And the response should contain store information

  @raw-request-executor @go-only
  Scenario: Execute raw POST request with body
    Given I have a client configured with raw request executor
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I execute a raw POST request to "/stores/01ARZ3NDEKTSV4RRFFQ69G5FAV/check" with body:
      """
      {
        "tuple_key": {
          "user": "user:alice",
          "relation": "viewer",
          "object": "document:doc1"
        }
      }
      """
    Then the response should be successful
    And the response should contain check result

  @raw-request-executor @go-only
  Scenario: Execute raw request with custom headers
    Given I have a client configured with raw request executor
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I execute a raw POST request to "/stores/01ARZ3NDEKTSV4RRFFQ69G5FAV/check" with headers:
      | X-Custom-Header | test-value |
      | Authorization   | Bearer token-123 |
    And body:
      """
      {
        "tuple_key": {
          "user": "user:alice",
          "relation": "viewer",
          "object": "document:doc1"
        }
      }
      """
    Then the response should be successful
    And the request should have included the custom headers

  @raw-request-executor @go-only
  Scenario: Execute raw request to non-standard endpoint
    Given I have a client configured with raw request executor
    When I execute a raw GET request to "/health"
    Then the response should be successful
    And the response should contain health status

  @raw-request-executor @go-only
  Scenario: Execute raw request with error handling
    Given I have a client configured with raw request executor
    When I execute a raw GET request to "/invalid/endpoint"
    Then the response should fail
    And the response should have status code 404
    And the raw executor should handle the error appropriately
