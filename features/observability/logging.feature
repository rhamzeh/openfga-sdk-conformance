Feature: Logging

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @logging
  Scenario: Basic logging configuration
    Given I have a client configured with logging enabled
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And log entries should be generated
    And the logs should include request details

  @logging
  Scenario: Custom log level configuration
    Given I have a client configured with log level "DEBUG"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And debug log entries should be generated
    And the logs should include detailed request information

  @logging
  Scenario: Structured logging with correlation ID
    Given I have a client configured with structured logging
    And I set the request header "X-Correlation-ID" to "corr-123"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the logs should include the correlation ID
    And the logs should be in structured format
