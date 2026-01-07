Feature: Telemetry Integration

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @telemetry
  Scenario: Basic telemetry collection
    Given telemetry is enabled
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then telemetry data should be collected
    And metrics should include request duration
    And metrics should include response status

  @telemetry
  Scenario: Custom metrics collection
    Given I configure custom metrics
    When I perform multiple API operations
    Then custom metrics should be recorded
    And metrics should be exportable

  @telemetry @prometheus
  Scenario: Prometheus metrics integration
    Given Prometheus metrics are enabled
    When I make 100 API calls across different endpoints
    Then Prometheus metrics should be exposed
    And metrics should include:
      | metric_name                    | metric_type | labels                    |
      | openfga_requests_total         | counter     | method, endpoint, status  |
      | openfga_request_duration       | histogram   | method, endpoint          |
      | openfga_active_connections     | gauge       | client_id                 |
      | openfga_errors_total           | counter     | error_type, endpoint      |

  @telemetry @custom
  Scenario: Custom telemetry provider integration
    Given I configure a custom telemetry provider
    When I perform API operations with custom attributes:
      | operation | custom_attribute | value        |
      | Check     | user_type        | internal     |
      | Write     | batch_size       | 50           |
      | Read      | filter_type      | user_filter  |
    Then custom attributes should be included in telemetry
    And telemetry should be sent to custom provider

  @telemetry @sampling
  Scenario: Telemetry sampling configuration
    Given I configure telemetry sampling at 10%
    When I make 1000 API requests
    Then approximately 100 requests should be sampled
    And sampling should be evenly distributed
    And performance impact should be minimal

  @telemetry @correlation
  Scenario: Request correlation tracking
    Given correlation IDs are enabled
    When I make a series of related API calls:
      | operation | correlation_id |
      | Check     | req-001        |
      | Write     | req-001        |
      | Read      | req-001        |
    Then all telemetry should include the correlation ID
    And related requests should be traceable

  @telemetry @performance
  Scenario: Telemetry performance impact
    Given telemetry is enabled with full collection
    When I run performance tests with and without telemetry
    Then telemetry overhead should be less than 5%
    And memory usage should not increase significantly
    And request latency should not be affected

  @telemetry @export
  Scenario: Telemetry data export formats
    Given telemetry collection is active
    When I export telemetry data
    Then data should be available in multiple formats:
      | format | content_type       |
      | JSON   | application/json   |
      | CSV    | text/csv           |
      | Parquet| application/parquet|
    And exported data should include all collected metrics
    And telemetry data should be sent to the custom endpoint
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And telemetry data should be collected
    And the telemetry should include request metrics

  @telemetry
  Scenario: Custom telemetry configuration
    Given I have a client configured with custom telemetry:
      | enabled | true |
      | metrics | true |
      | tracing | true |
      | endpoint | https://telemetry.example.com |
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And telemetry data should be sent to the custom endpoint
    And both metrics and tracing should be included

  @telemetry
  Scenario: Telemetry with error scenarios
    Given I have a client configured with telemetry enabled
    When I call Check with:
      | user     |               |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And telemetry data should include error information
    And the error should be properly categorized in telemetry

  @telemetry
  Scenario: Telemetry disabled
    Given I have a client configured with telemetry disabled
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And no telemetry data should be collected
