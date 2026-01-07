Feature: ReadLatestAuthorizationModel API - Client-side convenience method

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @read-latest-authorization-model
  Scenario: ReadLatestAuthorizationModel basic request
    When I call ReadLatestAuthorizationModel
    Then the response should be successful
    And the response should contain the latest authorization model
    And the authorization model should have an ID
    And the authorization model should have type definitions

  @read-latest-authorization-model
  Scenario: ReadLatestAuthorizationModel with custom headers
    Given I set the request header "X-Custom-Header" to "test-value"
    When I call ReadLatestAuthorizationModel
    Then the response should be successful
    And the response should contain the latest authorization model
    And the request should have been made with custom headers

  @read-latest-authorization-model
  Scenario: ReadLatestAuthorizationModel caching behavior
    When I call ReadLatestAuthorizationModel
    And I call ReadLatestAuthorizationModel again
    Then both responses should be successful
    And both responses should contain the same authorization model ID
