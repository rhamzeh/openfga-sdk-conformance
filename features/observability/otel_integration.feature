Feature: OpenTelemetry Integration

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @otel-integration
  Scenario: OpenTelemetry metrics collection
    Given I have a client configured with OpenTelemetry metrics:
      | enabled  | true |
      | endpoint | http://otel-collector:4317 |
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And OpenTelemetry metrics should be exported
    And the metrics should include request duration and count

  @otel-integration
  Scenario: OpenTelemetry tracing with custom attributes
    Given I have a client configured with OpenTelemetry tracing:
      | enabled    | true |
      | endpoint   | http://otel-collector:4317 |
      | attributes | service.name=openfga-sdk |
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And OpenTelemetry traces should be exported
    And the traces should include custom attributes
    And the spans should have proper parent-child relationships

  @otel-integration
  Scenario: OpenTelemetry with distributed tracing
    Given I have a client configured with OpenTelemetry tracing
    And I set the request header "traceparent" to "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the trace should be properly linked to the parent trace
    And the trace context should be propagated correctly
