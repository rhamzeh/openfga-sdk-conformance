Feature: ReadAssertions API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @read-assertions
  Scenario: Read assertions for authorization model
    When I call ReadAssertions for authorization model "01G50QVV17PECNVAHX1GG4Y5NC"
    Then the response should be successful
    And the response should contain assertions
    And each assertion should have a tuple key and expectation

  @read-assertions
  Scenario: Read assertions with custom headers
    Given I set the request header "X-Custom-Header" to "test-value"
    When I call ReadAssertions for authorization model "01G50QVV17PECNVAHX1GG4Y5NC"
    Then the response should be successful
    And the response should contain assertions
    And the request should have been made with custom headers

  @read-assertions
  Scenario: Read assertions for non-existent model
    When I call ReadAssertions for authorization model "non-existent-model-id"
    Then the response should fail
    And the response should have status code 404
    And the error message should contain "authorization model not found"

  @read-assertions
  Scenario: Read assertions for model with no assertions
    When I call ReadAssertions for authorization model "01G50QVV17PECNVAHX1GG4Y5NC"
    Then the response should be successful
    And the response should contain no assertions
