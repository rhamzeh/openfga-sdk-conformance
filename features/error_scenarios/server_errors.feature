Feature: Server Errors

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @server-errors
  Scenario: Server error
    Given I set the request header "X-Test-Scenario" to "server_error"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 500
    And the error message should contain "internal server error"

  @server-errors
  Scenario: Service unavailable error
    Given I set the request header "X-Test-Scenario" to "service_unavailable"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the response should have status code 503
    And the error message should contain "service unavailable"

  @server-errors
  Scenario: Timeout error
    Given I set the request header "X-Test-Scenario" to "timeout"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should fail
    And the error message should contain "timeout"
