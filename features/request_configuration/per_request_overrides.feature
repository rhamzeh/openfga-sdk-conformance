Feature: Per-Request Overrides

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @per-request-overrides
  Scenario: Override store ID per request
    Given I have a client configured with default store ID "01ARZ3NDEKTSV4RRFFQ69G5FAV"
    When I call Check with store ID "01BRZ3NDEKTSV4RRFFQ69G5FAV" and:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have used store ID "01BRZ3NDEKTSV4RRFFQ69G5FAV"

  @per-request-overrides
  Scenario: Override authorization model ID per request
    Given I have a client configured with default authorization model ID "01G50QVV17PECNVAHX1GG4Y5NC"
    When I call Check with authorization model ID "01H50QVV17PECNVAHX1GG4Y5NC" and:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have used authorization model ID "01H50QVV17PECNVAHX1GG4Y5NC"

  @per-request-overrides
  Scenario: Override consistency per request
    When I call Check with consistency "HIGHER_CONSISTENCY" and:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have used higher consistency

  @per-request-overrides
  Scenario: Override timeout per request
    When I call Check with timeout 30000ms and:
      | user     | user:alice    |
      | relation | viewer        |
      | object   | document:doc1 |
    Then the response should be successful
    And the request should have used the custom timeout
