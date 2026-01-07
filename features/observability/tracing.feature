Feature: Tracing

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @tracing
  Scenario: Basic tracing collection
    Given I have a client configured with tracing enabled
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And tracing data should be collected
    And the trace should include request spans

  @tracing
  Scenario: Distributed tracing with trace ID
    Given I have a client configured with tracing enabled
    And I set the request header "X-Trace-ID" to "trace-123"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the trace should include the custom trace ID
    And the trace should be properly propagated

  @tracing
  Scenario: Tracing with nested operations
    Given I have a client configured with tracing enabled
    When I perform nested operations:
      | operation   | parameters |
      | Check       | user:alice, viewer, document:doc1 |
      | ListObjects | user:alice, viewer, document |
      | BatchCheck  | multiple checks |
    Then all operations should be successful
    And the trace should show nested spans
    And each operation should have its own span
