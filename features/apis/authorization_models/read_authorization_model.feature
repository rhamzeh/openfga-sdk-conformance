Feature: ReadAuthorizationModel API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @read-authorization-model
  Scenario: Read specific authorization model by ID
    When I call ReadAuthorizationModel with ID "01G50QVV17PECNVAHX1GG4Y5NC"
    Then the response should be successful
    And the response should contain the authorization model
    And the model ID should be "01G50QVV17PECNVAHX1GG4Y5NC"
    And the model should have type definitions

  @read-authorization-model
  Scenario: Read authorization model with custom headers
    Given I set the request header "X-Custom-Header" to "test-value"
    When I call ReadAuthorizationModel with ID "01G50QVV17PECNVAHX1GG4Y5NC"
    Then the response should be successful
    And the response should contain the authorization model
    And the request should have been made with custom headers

  @read-authorization-model
  Scenario: Read non-existent authorization model
    When I call ReadAuthorizationModel with ID "non-existent-model-id"
    Then the response should fail
    And the response should have status code 404
    And the error message should contain "authorization model not found"
