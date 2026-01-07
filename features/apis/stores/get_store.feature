Feature: GetStore API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @get-store
  Scenario: Get existing store
    When I call GetStore
    Then the response should be successful
    And the response should contain store details
    And the store ID should be "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @get-store
  Scenario: Get store with custom headers
    Given I set the request header "X-Custom-Header" to "test-value"
    When I call GetStore
    Then the response should be successful
    And the request should have been made with custom headers
