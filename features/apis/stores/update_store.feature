Feature: UpdateStore API

  Background:
    Given I have a client configured with store "01ARZ3NDEKTSV4RRFFQ69G5FAV"

  @update-store @unimplemented
  Scenario: Update store name
    When I call UpdateStore with name "updated-store-name"
    Then the response should fail
    And the response should have status code 501
    And the error message should contain "not implemented"

  @update-store @unimplemented
  Scenario: Update store with custom headers
    Given I set the request header "X-Custom-Header" to "test-value"
    When I call UpdateStore with name "updated-store-name"
    Then the response should fail
    And the response should have status code 501
    And the error message should contain "not implemented"
