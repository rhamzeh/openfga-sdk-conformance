Feature: Cross-Feature Interactions

  @integration @cross-feature
  Scenario: Authentication with retry and telemetry
    Given I have a client configured with:
      | authentication | OIDC with token refresh |
      | retry_policy   | exponential backoff     |
      | telemetry      | enabled                 |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with intermittent server errors:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should eventually be successful
    And retries should be performed with authentication
    And telemetry should capture retry attempts
    And authentication tokens should be refreshed if needed

  @integration @cross-feature
  Scenario: Streaming with custom headers and error handling
    Given I have a client configured with:
      | default_headers | X-Client-Version: 1.0.0 |
      | retry_policy    | enabled                  |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call StreamedListObjects with:
      | user     | user:alice |
      | relation | viewer     |
      | type     | document   |
    And the stream encounters temporary errors
    Then the stream should recover automatically
    And custom headers should be included in all requests
    And error recovery should be transparent to the client

  @integration @cross-feature
  Scenario: Performance testing with observability
    Given I have a client configured with:
      | telemetry | enabled with custom endpoint |
      | tracing   | enabled                      |
      | logging   | debug level                  |
    When I perform high-volume concurrent operations:
      | operation | count | concurrent |
      | Check     | 100   | 20         |
      | Write     | 50    | 10         |
    Then all operations should complete successfully
    And telemetry data should capture performance metrics
    And traces should show operation timings
    And logs should include detailed operation information
    And no performance degradation should occur due to observability
