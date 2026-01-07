Feature: Base Configuration

  @base-configuration
  Scenario: Client with custom API URL
    Given I have a client configured with API URL "https://custom.openfga.example"
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been made to "https://custom.openfga.example"

  @base-configuration
  Scenario: Client with custom API URL and path
    Given I have a client configured with API URL "https://api.example.com/openfga/v1"
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have been made to "https://api.example.com/openfga/v1"

  @base-configuration
  Scenario: Client with default store ID
    Given I have a client configured with default store ID "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have used the default store ID

  @base-configuration
  Scenario: Client with default authorization model ID
    Given I have a client configured with default authorization model ID "01G50QVV17PECNVAHX1GG4Y5NC"
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have used the default authorization model ID

  @base-configuration
  Scenario: Client with telemetry configuration
    Given I have a client configured with telemetry:
      | enabled | true |
      | metrics | true |
      | tracing | true |
    And I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And telemetry data should have been collected
